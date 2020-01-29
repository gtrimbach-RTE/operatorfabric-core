/* Copyright (c) 2020, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */


package org.lfenergy.operatorfabric.cards.consultation.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.operatorfabric.users.model.User;
import org.lfenergy.operatorfabric.utilities.VirtualTime;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Instant;

/**
 * <p>This object manages subscription to AMQP exchange</p>
 *
 * <p>Two exchanges are used, {@link #groupExchange} and {@link #userExchange}.
 * See amqp.xml resource file ([project]/services/core/cards-publication/src/main/resources/amqp.xml)
 * for their exact configuration</p>
 *
 * @author David Binder
 */
@Slf4j
@EqualsAndHashCode
public class CardSubscription {
    public static final String GROUPS_SUFFIX = "Groups";
    private String userQueueName;
    private String groupQueueName;
    private long current = 0;
    @Getter
    private User user;
    @Getter
    private String id;
    @Getter
    private Flux<String> publisher;
    private Flux<String> amqpPublisher;
    private EmitterProcessor<String> externalPublisher;
    private Flux<String> externalFlux;
    private FluxSink<String> externalSink;
    private AmqpAdmin amqpAdmin;
    private DirectExchange userExchange;
    private TopicExchange groupExchange;
    private ConnectionFactory connectionFactory;
    private MessageListenerContainer userMlc;
    private MessageListenerContainer groupMlc;
    @Getter
    @JsonInclude
    private Instant rangeStart;
    @Getter
    @JsonInclude
    private Instant rangeEnd;
    private boolean filterNotification;
    @Getter
    private Instant startingPublishDate;
    @Getter
    private boolean cleared = false;
    private final String clientId;

    /**
     * Constructs a card subscription and init access to AMQP exchanges
     * @param user connected user
     * @param clientId id of client (generated by ui)
     * @param doOnCancel a runnable to call on subscription cancellation
     * @param amqpAdmin AMQP management component
     * @param userExchange configured exchange for orphaned user messages
     * @param groupExchange configured exchange for group messages
     * @param connectionFactory AMQP connection  factory to instantiate listeners
     */
    @Builder
    public CardSubscription(User user,
                            String clientId,
                            Runnable doOnCancel,
                            AmqpAdmin amqpAdmin,
                            DirectExchange userExchange,
                            TopicExchange groupExchange,
                            ConnectionFactory connectionFactory,
                            Instant rangeStart,
                            Instant rangeEnd,
                            Boolean filterNotification) {
        if(user!=null)
            this.id = computeSubscriptionId(user.getLogin(), clientId);
        this.user = user;
        this.amqpAdmin = amqpAdmin;
        this.userExchange = userExchange;
        this.groupExchange = groupExchange;
        this.connectionFactory = connectionFactory;
        this.clientId = clientId;
        if(user!=null) {
            this.userQueueName = computeSubscriptionId(user.getLogin(), this.clientId);
            this.groupQueueName = computeSubscriptionId(user.getLogin() + GROUPS_SUFFIX, this.clientId);
        }
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.filterNotification = filterNotification!=null && filterNotification;
    }

    public static String computeSubscriptionId(String prefix, String clientId) {
        return prefix + "#" + clientId;
    }

    /**
     * <ul>
     * <li>Create a user queue and a group topic queue</li>
     * <li>Associate queues to message {@link MessageListenerContainer}.</li>
     * <li>Creates a amqpPublisher {@link Flux} to publish AMQP messages to</li>
     * </ul>
     * <p>
     * Listeners starts on {@link Flux} subscription.
     * </p>
     * <p>On subscription cancellation triggers doOnCancel</p>
     * @param doOnCancel
     */
    public void initSubscription(Runnable doOnCancel) {
        createUserQueue();
        createTopicQueue();
        this.userMlc = createMessageListenerContainer(this.userQueueName);
        this.groupMlc = createMessageListenerContainer(groupQueueName);
        amqpPublisher = Flux.create(emitter -> {
            registerListener(userMlc, emitter,this.user.getLogin());
            registerListener(groupMlc, emitter,this.user.getLogin()+ GROUPS_SUFFIX);
            emitter.onRequest(v -> {
                log.info("STARTING subscription");
                log.info(String.format("LISTENING to messages on User[%s] queue",this.user.getLogin()));
                userMlc.start();
                log.info(String.format("LISTENING to messages on Group[%sGroups] queue",this.user.getLogin()));
                groupMlc.start();
                startingPublishDate = VirtualTime.getInstance().computeNow();
            });
            emitter.onDispose(()->{
                log.info("DISPOSING amqp publisher");
                doOnCancel.run();
            });
        });
        this.externalPublisher = EmitterProcessor.create();
        this.externalSink = this.externalPublisher.sink();
        this.amqpPublisher = amqpPublisher
                .doOnError(t->log.error("ERROR on amqp publisher",t))
                .doOnComplete(()->log.info("COMPLETE amqp Publisher"))
                .doOnCancel(()->log.info("CANCELED amqp publisher"));
        this.externalFlux = this.externalPublisher
                .doOnError(t->log.error("ERROR on external publisher",t))
                .doOnComplete(()->log.info("COMPLETE external Publisher"))
                .doOnCancel(()->log.info("CANCELED external publisher"));
        this.publisher = amqpPublisher.mergeWith(externalFlux)
                .doOnError(t->log.error("ERROR on merged publisher",t))
                .doOnComplete(()->log.info("COMPLETE merged publisher"))
                .doOnCancel(()->log.info("CANCELED merged publisher"));
    }

    /**
     * Creates a message listener which publishes messages to {@link FluxSink}
     *
     * @param userMlc
     * @param emitter
     * @param queueName
     */
    private void registerListener(MessageListenerContainer userMlc, FluxSink<String> emitter, String queueName) {
        userMlc.setupMessageListener((MessageListener) message -> {
            log.info("PUBLISHING message from "+queueName);
            emitter.next(new String(message.getBody()));

        });
    }

    /**
     * Constructs a non durable queue to userExchange using user login as binding, queue name
     * is [user login]#[client id]
     * @return
     */
    private Queue createUserQueue() {
        log.info(String.format("CREATE User[%s] queue",this.user.getLogin()));
        Queue queue = QueueBuilder.nonDurable(this.userQueueName).build();
        amqpAdmin.declareQueue(queue);
        Binding binding = BindingBuilder
           .bind(queue)
           .to(this.userExchange)
           .with(this.user.getLogin());
        amqpAdmin.declareBinding(binding);
        log.info(String.format("CREATED User[%s] queue",this.userQueueName));
        return queue;
    }

    /**
     * <p>Constructs a non durable queue to groupExchange using queue name
     * [user login]Groups#[client id].</p>
     * <p>Generates a binding for each group using pattern: #.[group].#</p>
     * @return
     */
    private Queue createTopicQueue() {
        log.info(String.format("CREATE Group[%sGroups] queue",this.user.getLogin()));
        Queue queue = QueueBuilder.nonDurable(this.groupQueueName).build();
        amqpAdmin.declareQueue(queue);
        this.user.getGroups().stream().map(g -> "#." + g + ".#").forEach(g -> {
            Binding binding = BindingBuilder.bind(queue).to(groupExchange).with(g);
            amqpAdmin.declareBinding(binding);
        });
        log.info(String.format("CREATED Group[%sGroups] queue with bindings :",this.groupQueueName));
        this.user.getGroups().stream().map(g -> "#." + g + ".#").forEach(g -> log.info("\t* "+g));
        return queue;
    }

    /**
     * Stops associated {@link MessageListenerContainer} and delete queues
     */
    public void clearSubscription() {
        log.info(String.format("STOPPING User[%s] queue",this.userQueueName));
        this.userMlc.stop();
        amqpAdmin.deleteQueue(this.userQueueName);
        log.info(String.format("STOPPING Group[%sGroups] queue",this.groupQueueName));
        this.groupMlc.stop();
        amqpAdmin.deleteQueue(this.groupQueueName);
        this.cleared = true;
    }

    /**
     *
     * @return true if associated AMQP listeners are still running
     */
    public boolean checkActive(){
        boolean userActive = userMlc == null || userMlc.isRunning();
        boolean groupActive = groupMlc == null || groupMlc.isRunning();
        return userActive && groupActive;
    }


    /**
     * Create a {@link MessageListenerContainer} for the specified queue
     * @param queueName AMQP queue name
     * @return listener container for the specified queue
     */
    public MessageListenerContainer createMessageListenerContainer(String queueName) {

        SimpleMessageListenerContainer mlc = new SimpleMessageListenerContainer(connectionFactory);
        mlc.addQueueNames(queueName);
        mlc.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return mlc;
    }

    public void updateRange(Instant rangeStart, Instant rangeEnd) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        startingPublishDate = VirtualTime.getInstance().computeNow();
    }

    public void publishInto(Flux<String> fetchOldCards) {
        fetchOldCards.subscribe(next->this.externalSink.next(next));
    }
}

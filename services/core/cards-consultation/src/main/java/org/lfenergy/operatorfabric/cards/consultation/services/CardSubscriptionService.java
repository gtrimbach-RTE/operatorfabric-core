/* Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the OperatorFabric project.
 */



package org.lfenergy.operatorfabric.cards.consultation.services;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.operatorfabric.users.model.CurrentUserWithPerimeters;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * <p>Centralize request for generating {@link CardSubscription} and deleting them.</p>
 *
 * <p>Uses a {@link ThreadPoolTaskScheduler} delay definitive deletion of subscription (defaults to 10s)</p>
 *
 *
 */
@Service
@Slf4j
public class CardSubscriptionService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final FanoutExchange cardExchange;
    private final AmqpAdmin amqpAdmin;
    private final long deletionDelay;
    private final ConnectionFactory connectionFactory;
    private Map<String, CardSubscription> cache = new ConcurrentHashMap<>();
    private Map<String, ScheduledFuture<?>> pendingEvict = new ConcurrentHashMap<>();

    @Autowired
    public CardSubscriptionService(ThreadPoolTaskScheduler taskScheduler,
                                   FanoutExchange cardExchange,
                                   ConnectionFactory connectionFactory,
                                   AmqpAdmin amqpAdmin,
                                   @Value("${opfab.subscriptiondeletion.delay:10000}")
                                      long deletionDelay) {
        this.cardExchange = cardExchange;
        this.taskScheduler = taskScheduler;
        this.amqpAdmin = amqpAdmin;
        this.connectionFactory = connectionFactory;
        this.deletionDelay = deletionDelay;
    }

    /**
     * <p>Generates a {@link CardSubscription} or retrieve it from a local {@link CardSubscription} cache.</p>
     * <p>If it finds a {@link CardSubscription} from cache, it will try to cancel possible scheduled evict</p>
     */
    public synchronized CardSubscription subscribe(
            CurrentUserWithPerimeters currentUserWithPerimeters,
            String clientId) {
        String subId = CardSubscription.computeSubscriptionId(currentUserWithPerimeters.getUserData().getLogin(), clientId);
        CardSubscription cardSubscription = cache.get(subId);
        // The builder may seem declare a bit to early but it allows usage in both branch of the later condition
        CardSubscription.CardSubscriptionBuilder cardSubscriptionBuilder = CardSubscription.builder()
           .currentUserWithPerimeters(currentUserWithPerimeters)
           .clientId(clientId)
           .amqpAdmin(amqpAdmin)
           .cardExchange(this.cardExchange)
           .connectionFactory(this.connectionFactory);
        if (cardSubscription == null) {
            cardSubscription = buildSubscription(subId, cardSubscriptionBuilder);
        } else {
            if (!cancelEviction(subId)) {
                cardSubscription = cache.get(subId);
                if (cardSubscription == null) {
                    cardSubscription = buildSubscription(subId, cardSubscriptionBuilder);
                }
            }
        }
        return cardSubscription;
    }

    private CardSubscription buildSubscription(String subId, CardSubscription.CardSubscriptionBuilder cardSubscriptionBuilder) {
        CardSubscription cardSubscription;
        cardSubscription = cardSubscriptionBuilder.build();
        cardSubscription.initSubscription(() -> scheduleEviction(subId));
        cache.put(subId, cardSubscription);
        log.debug("Subscription created with id {}", cardSubscription.getId());
        return cardSubscription;
    }

    /**
     * Schedule deletion of subscription in deletionDelay millis
     *
     * @param subId
     *    Subscription computed id
     */
    public void scheduleEviction(String subId) {
        if (!pendingEvict.containsKey(subId)) {
            ScheduledFuture<?> scheduled = taskScheduler.schedule(createEvictTask(subId),
               new Date(System.currentTimeMillis() + deletionDelay));
            pendingEvict.put(subId, scheduled);
            log.debug("Eviction scheduled for id {}", subId);
        }
    }

    /**
     * Cancel scheduled evict if any
     *
     * @param subId
     *    subscription auto-generated id
     * @return true if eviction was successfully cancelled, false may indicate that either no cancellation was
     * possible or no eviction was previously scheduled
     */
    public synchronized boolean cancelEviction(String subId) {
        ScheduledFuture<?> scheduled = pendingEvict.get(subId);
        if (scheduled != null) {
            boolean canceled = scheduled.cancel(false);
            pendingEvict.remove(subId);
            log.debug("Eviction canceled with id {}", subId);
            return canceled;
        }
        return false;
    }

    /**
     * Evict subscription definitively
     *
     * @param subId subscription unique id
     *    subscription autogenerated id
     */
    public synchronized void evict(String subId) {
        log.debug("Trying to evict subscription with id {}", subId);
        cache.get(subId).clearSubscription();
        cache.remove(subId);
        pendingEvict.remove(subId);
        log.debug("Subscription with id {} evicted ", subId);
    }

    /**
     * Create a runnable to to launch {@link #evict(String)}
     *
     * @param subId
     *    subscription autogenerated id
     * @return the generated task
     */
    private Runnable createEvictTask(String subId) {
        return () -> evict(subId);
    }

    /**
     * <p>Find existing subscription</p>
     * <p>NB: May throw {@link IllegalStateException} if any of the parameters is missing</p>
     * @param currentUserWithPerimeters Users whom subscription we search
     * @param uiId Unique client id whom subscription we search
     * @return
     */
    public CardSubscription findSubscription(CurrentUserWithPerimeters currentUserWithPerimeters, String uiId) {
        if(currentUserWithPerimeters == null)
            throw new IllegalArgumentException("user is a mandatory parameter of CardSubscriptionService#findSubscription");
        if(uiId == null)
            throw new IllegalArgumentException("uiId is a mandatory parameter of CardSubscriptionService#findSubscription");
        String subId = CardSubscription.computeSubscriptionId(currentUserWithPerimeters.getUserData().getLogin(), uiId);
        return cache.get(subId);
    }

    public void clearSubscriptions() {
        this.cache.forEach((k,v)->v.clearSubscription());
        this.cache.clear();
        this.pendingEvict.clear();
    }
}

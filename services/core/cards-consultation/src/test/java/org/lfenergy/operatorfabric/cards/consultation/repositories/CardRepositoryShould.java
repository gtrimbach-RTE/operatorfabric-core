/* Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the OperatorFabric project.
 */

package org.lfenergy.operatorfabric.cards.consultation.repositories;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.operatorfabric.cards.consultation.TestUtilities;
import org.lfenergy.operatorfabric.cards.consultation.application.IntegrationTestApplication;
import org.lfenergy.operatorfabric.cards.consultation.model.*;
import org.lfenergy.operatorfabric.cards.model.RecipientEnum;
import org.lfenergy.operatorfabric.cards.model.SeverityEnum;
import org.lfenergy.operatorfabric.cards.model.TitlePositionEnum;
import org.lfenergy.operatorfabric.users.model.CurrentUserWithPerimeters;
import org.lfenergy.operatorfabric.users.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.operatorfabric.cards.consultation.TestUtilities.createSimpleCard;
import static org.lfenergy.operatorfabric.cards.consultation.TestUtilities.prepareCard;


/**
 * <p></p>
 * Created on 24/07/18
 *
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = IntegrationTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"native", "test"})
//@Disabled
@Tag("end-to-end")
@Tag("mongo")
@Slf4j
public class CardRepositoryShould {

    public static final String LOGIN = "admin";
    private static Instant now = Instant.now();
    private static Instant nowPlusOne = now.plus(1, ChronoUnit.HOURS);
    private static Instant nowPlusTwo = now.plus(2, ChronoUnit.HOURS);
    private static Instant nowPlusThree = now.plus(3, ChronoUnit.HOURS);
    private static Instant nowMinusOne = now.minus(1, ChronoUnit.HOURS);
    private static Instant nowMinusTwo = now.minus(2, ChronoUnit.HOURS);
    private static Instant nowMinusThree = now.minus(3, ChronoUnit.HOURS);

    @Autowired
    private CardRepository repository;


    private CurrentUserWithPerimeters rteUserEntity1,rteUserEntity2, adminUser;


    public CardRepositoryShould(){
        User user1 = new User();
        user1.setLogin("rte-operator");
        user1.setFirstName("Test");
        user1.setLastName("User");
        List<String> groups = new ArrayList<>();
        groups.add("rte");
        groups.add("operator");
        user1.setGroups(groups);
        List<String> entities1 = new ArrayList<>();
        entities1.add("entity1");
        user1.setEntities(entities1);
        rteUserEntity1 = new CurrentUserWithPerimeters();
        rteUserEntity1.setUserData(user1);

        User user2 = new User();
        user2.setLogin("rte-operator");
        user2.setFirstName("Test");
        user2.setLastName("User");
        List<String> groups2 = new ArrayList<>();
        groups2.add("rte");
        groups2.add("operator");
        user2.setGroups(groups2);
        List<String> entities2 = new ArrayList<>();
        entities2.add("entity2");
        user2.setEntities(entities2);
        rteUserEntity2 = new CurrentUserWithPerimeters();
        rteUserEntity2.setUserData(user2);
        
        User user3 = new User();
        user3.setLogin("admin");
        user3.setFirstName("Test");
        user3.setLastName("User");;
        adminUser = new CurrentUserWithPerimeters();
        adminUser.setUserData(user3);
    }

    @AfterEach
    public void clean() {
        repository.deleteAll().subscribe();
    }

    @BeforeEach
    private void initCardData() {
        int processNo = 0;
        //create past cards
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowMinusTwo, nowMinusOne, LOGIN, new String[]{"rte","operator"}, new String[]{"entity1", "entity2"}));
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowMinusTwo, nowMinusOne, LOGIN, new String[]{"rte","operator"}, null));
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowMinusOne, now, LOGIN, new String[]{"rte","operator"}, null, new String[]{"any-operator","some-operator"},new String[]{"rte-operator","some-operator"}));
        //create future cards
        persistCard(createSimpleCard(processNo++, nowMinusThree, now, nowPlusOne, LOGIN, new String[]{"rte","operator"}, null));
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowPlusOne, nowPlusTwo, LOGIN, new String[]{"rte","operator"}, new String[]{"entity1", "entity2"}, new String[]{"rte-operator","some-operator"}, new String[]{"any-operator","some-operator"}));
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowPlusTwo, nowPlusThree, LOGIN, new String[]{"rte","operator"}, null));

        //card starts in past and ends in future
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowMinusThree, nowPlusThree, LOGIN, new String[]{"rte","operator"}, null));

        //card starts in past and never ends
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowMinusThree, null, LOGIN, new String[]{"rte","operator"}, null, null, new String[]{"rte-operator","some-operator"}));

        //card starts in future and never ends
        persistCard(createSimpleCard(processNo++, nowMinusThree, nowPlusThree, null, LOGIN, new String[]{"rte","operator"}, null));

        //create later published cards in past
        //this one overrides first
        persistCard(createSimpleCard(1, nowPlusOne, nowMinusTwo, nowMinusOne, LOGIN, new String[]{"rte","operator"}, null));
        persistCard(createSimpleCard(processNo++, nowPlusOne, nowMinusTwo, nowMinusOne, LOGIN, new String[]{"rte","operator"}, null));
        //create later published cards in future
        // this one overrides businessconfig
        persistCard(createSimpleCard(3, nowPlusOne, nowPlusOne, nowPlusTwo, LOGIN, new String[]{"rte","operator"}, null));
        persistCard(createSimpleCard(processNo++, nowPlusOne, nowPlusTwo, nowPlusThree, LOGIN, new String[]{"rte","operator"}, null));
    }

    private void persistCard(CardConsultationData simpleCard) {
        StepVerifier.create(repository.save(simpleCard))
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }
   

    @Test
    public void persistCard() {
        CardConsultationData card =
                CardConsultationData.builder()
                        .processInstanceId("PROCESS_ID")
                        .process("PROCESS")
                        .publisher("PUBLISHER")
                        .processVersion("0")
                        .state("anyState")
                        .startDate(Instant.now())
                        .severity(SeverityEnum.ALARM)
                        .title(I18nConsultationData.builder().key("title").build())
                        .summary(I18nConsultationData.builder().key("summary").build())
                        .recipient(RecipientConsultationData.builder()
                                .type(RecipientEnum.UNION)
                                .recipient(RecipientConsultationData.builder()
                                        .type(RecipientEnum.GROUP)
                                        .identity("group1")
                                        .build())
                                .recipient(RecipientConsultationData.builder()
                                        .type(RecipientEnum.GROUP)
                                        .identity("group2")
                                        .build())
                                .build())
                        .detail(DetailConsultationData.builder()
                                .templateName("template")
                                .title(I18nConsultationData.builder()
                                        .key("key")
                                        .parameter("param1", "value1")
                                        .build())
                                .titlePosition(TitlePositionEnum.UP)
                                .style("style")
                                .build())
                        .entityRecipients(new ArrayList<String>(Arrays.asList("entity1", "entity2")))
                        .build();
        prepareCard(card, Instant.now());
        StepVerifier.create(repository.save(card))
                .expectNextMatches(computeCardPredicate(card))
                .expectComplete()
                .verify();

        StepVerifier.create(repository.findById("PROCESS.PROCESS_ID"))
                .expectNextMatches(computeCardPredicate(card))
                .expectComplete()
                .verify();
    }

    @Test
    public void fetchPast() {
        //matches rte group and entity1
        log.info(String.format("Fetching past before now(%s), published after now(%s)", TestUtilities.format(now), TestUtilities.format(now)));
        StepVerifier.create(repository.getCardOperations(now, null,now, rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertCard(op,"PROCESS.PROCESS0", "PUBLISHER", "0");
                })
                .expectComplete()
                .verify();


        //matches admin  user
        StepVerifier.create(repository.getCardOperations(now,null, now,adminUser)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertCard(op, "PROCESS.PROCESS0", "PUBLISHER", "0");
                })
                .expectComplete()
                .verify();

        log.info(String.format("Fetching past before now plus three hours(%s), published after now(%s)", TestUtilities.format(nowPlusThree), TestUtilities.format(now)));
        StepVerifier.create(repository.getCardOperations(now,null,nowPlusThree, rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertCard(op,"PROCESS.PROCESS0", "PUBLISHER", "0");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertCard(op,"PROCESS.PROCESS2", "PUBLISHER", "0");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertCard(op,"PROCESS.PROCESS4", "PUBLISHER", "0");
                })
                .expectComplete()
                .verify();
        log.info(String.format("Fetching past before now (%s), published after now plus three hours(%s)", TestUtilities.format(now), TestUtilities.format(nowPlusThree)));
        StepVerifier.create(repository.getCardOperations(nowPlusThree, null,now,rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertCard(op,"PROCESS.PROCESS0", "PUBLISHER", "0");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowPlusOne);
                    assertCard(op, "PROCESS.PROCESS1", "PUBLISHER", "0");
                })
                .assertNext(op -> {
                    assertThat(op.getPublishDate()).isEqualTo(nowPlusOne);
                    assertCard(op,"PROCESS.PROCESS9", "PUBLISHER", "0");
                })
                .expectComplete()
                .verify();
    }

    private void assertCard(CardOperation op,Object processName, Object publisher, Object processVersion) {
        assertThat(op.getCards().get(0).getId()).isEqualTo(processName);
        assertThat(op.getCards().get(0).getPublisher()).isEqualTo(publisher);
        assertThat(op.getCards().get(0).getProcessVersion()).isEqualTo(processVersion);
    }

    @Test
    public void fetchFuture() {
        log.info(String.format("Fetching future from now(%s), published after now(%s)", TestUtilities.format(now), TestUtilities.format(now)));
        StepVerifier.create(repository.getCardOperations(now, now,null, rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS5");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(2).getId()).isEqualTo("PROCESS.PROCESS8");
                });

        log.info(String.format("Fetching future from now minus two hours(%s), published after now(%s)", TestUtilities.format(nowMinusTwo), TestUtilities.format(now)));
        StepVerifier.create(repository.getCardOperations(now, nowMinusTwo,null,rteUserEntity2)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS2");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS5");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS8");
                })
                .expectComplete()
                .verify();
        log.info(String.format("Fetching future from now minus two hours(%s), published after now plus three hours(%s)", TestUtilities.format(nowMinusTwo), TestUtilities.format(nowPlusThree)));
        StepVerifier.create(repository.getCardOperations(nowPlusThree, nowMinusTwo, null,rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS2");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowPlusOne);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS3");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS5");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS8");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowPlusOne);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS10");
                })
                .expectComplete()
                .verify();
    }


    @Test
    public void fetchRange() {
        log.info(String.format("Fetching urgent from now minus one hours(%s) and now plus one hours(%s), published after now (%s)", TestUtilities.format(nowMinusOne), TestUtilities.format(nowPlusOne), TestUtilities.format(now)));
        StepVerifier.create(repository.getCardOperations(now, nowMinusOne, nowPlusOne, rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation))
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS0");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS2");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS6");
                })
                .assertNext(op -> {
                    assertThat(op.getCards().size()).isEqualTo(1);
                    assertThat(op.getPublishDate()).isEqualTo(nowMinusThree);
                    assertThat(op.getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS7");
                })

                .expectComplete()
                .verify();
    }
    
    @Test
    public void fetchPastAndCheckUserAcks() {
        log.info(String.format("Fetching past before now plus three hours(%s), published after now(%s)", TestUtilities.format(nowPlusThree), TestUtilities.format(now)));
        List<CardOperation> list = repository.getCardOperations(now, null,nowPlusThree, rteUserEntity1)
	        .doOnNext(TestUtilities::logCardOperation)
	        .filter(co -> Arrays.asList("PROCESS.PROCESS0","PROCESS.PROCESS2","PROCESS.PROCESS4").contains(co.getCards().get(0).getId()))
			.collectSortedList((co1,co2) -> co1.getCards().get(0).getId().compareTo(co2.getCards().get(0).getId())).block();
        assertThat(list.get(0).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS0");
        assertThat(list.get(0).getCards().get(0).getHasBeenAcknowledged()).isFalse();
        assertThat(list.get(1).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS2");
        assertThat(list.get(1).getCards().get(0).getHasBeenAcknowledged()).isFalse();
        assertThat(list.get(2).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
        assertThat(list.get(2).getCards().get(0).getHasBeenAcknowledged()).isTrue();
    }
    
    @Test
    public void fetchFutureAndCheckUserAcks() {
    	log.info(String.format("Fetching future from now minus two hours(%s), published after now(%s)", TestUtilities.format(nowMinusTwo), TestUtilities.format(now)));
    	List<CardOperation> list = repository.getCardOperations(now, nowMinusTwo, null, rteUserEntity2)
                .doOnNext(TestUtilities::logCardOperation)
                .filter(co -> Arrays.asList("PROCESS.PROCESS2","PROCESS.PROCESS4","PROCESS.PROCESS5").contains(co.getCards().get(0).getId()))
    			.collectSortedList((co1,co2) -> co1.getCards().get(0).getId().compareTo(co2.getCards().get(0).getId())).block();

    	assertThat(list.get(0).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS2");
        assertThat(list.get(0).getCards().get(0).getHasBeenAcknowledged()).isFalse();
        assertThat(list.get(1).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
        assertThat(list.get(1).getCards().get(0).getHasBeenAcknowledged()).isTrue();
        assertThat(list.get(2).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS5");
        assertThat(list.get(2).getCards().get(0).getHasBeenAcknowledged()).isFalse();              
    }
    
    @Test
    public void fetchRangeAndCheckUserAcks() {
        log.info(String.format("Fetching urgent from now minus one hours(%s) and now plus one hours(%s), published after now (%s)", TestUtilities.format(nowMinusOne), TestUtilities.format(nowPlusOne), TestUtilities.format(now)));
        List<CardOperation> list = repository.getCardOperations(now, nowMinusOne, nowPlusOne, rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation)
                .filter(co -> Arrays.asList("PROCESS.PROCESS4","PROCESS.PROCESS6","PROCESS.PROCESS7").contains(co.getCards().get(0).getId()))
    			.collectSortedList((co1,co2) -> co1.getCards().get(0).getId().compareTo(co2.getCards().get(0).getId())).block();
        assertThat(list.get(0).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
        assertThat(list.get(0).getCards().get(0).getHasBeenAcknowledged()).isTrue();
        assertThat(list.get(1).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS6");
        assertThat(list.get(1).getCards().get(0).getHasBeenAcknowledged()).isFalse();
        assertThat(list.get(2).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS7");
        assertThat(list.get(2).getCards().get(0).getHasBeenAcknowledged()).isFalse();

    }
    
    @Test
    public void fetchPastAndCheckUserReads() {
        log.info(String.format("Fetching past before now plus three hours(%s), published after now(%s)", TestUtilities.format(nowPlusThree), TestUtilities.format(now)));
        List<CardOperation> list = repository.getCardOperations(now, null,nowPlusThree, rteUserEntity1)
	        .doOnNext(TestUtilities::logCardOperation)
	        .filter(co -> Arrays.asList("PROCESS.PROCESS0","PROCESS.PROCESS2","PROCESS.PROCESS4").contains(co.getCards().get(0).getId()))
			.collectSortedList((co1,co2) -> co1.getCards().get(0).getId().compareTo(co2.getCards().get(0).getId())).block();
        assertThat(list.get(0).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS0");
        assertThat(list.get(0).getCards().get(0).getHasBeenRead()).isFalse();
        assertThat(list.get(1).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS2");
        assertThat(list.get(1).getCards().get(0).getHasBeenRead()).isTrue();
        assertThat(list.get(2).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
        assertThat(list.get(2).getCards().get(0).getHasBeenRead()).isFalse();
    }
    
    @Test
    public void fetchFutureAndCheckUserReads() {
    	log.info(String.format("Fetching future from now minus two hours(%s), published after now(%s)", TestUtilities.format(nowMinusTwo), TestUtilities.format(now)));
    	List<CardOperation> list = repository.getCardOperations(now, nowMinusTwo, null, rteUserEntity2)
                .doOnNext(TestUtilities::logCardOperation)
                .filter(co -> Arrays.asList("PROCESS.PROCESS2","PROCESS.PROCESS4","PROCESS.PROCESS5").contains(co.getCards().get(0).getId()))
    			.collectSortedList((co1,co2) -> co1.getCards().get(0).getId().compareTo(co2.getCards().get(0).getId())).block();

    	assertThat(list.get(0).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS2");
        assertThat(list.get(0).getCards().get(0).getHasBeenRead()).isTrue();
        assertThat(list.get(1).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
        assertThat(list.get(1).getCards().get(0).getHasBeenRead()).isFalse();
        assertThat(list.get(2).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS5");
        assertThat(list.get(2).getCards().get(0).getHasBeenRead()).isFalse();              
    }
    
    @Test
    public void fetchRangeAndCheckUserReads() {
        log.info(String.format("Fetching urgent from now minus one hours(%s) and now plus one hours(%s), published after now (%s)", TestUtilities.format(nowMinusOne), TestUtilities.format(nowPlusOne), TestUtilities.format(now)));
        List<CardOperation> list = repository.getCardOperations(now, nowMinusOne, nowPlusOne, rteUserEntity1)
                .doOnNext(TestUtilities::logCardOperation)
                .filter(co -> Arrays.asList("PROCESS.PROCESS4","PROCESS.PROCESS6","PROCESS.PROCESS7").contains(co.getCards().get(0).getId()))
    			.collectSortedList((co1,co2) -> co1.getCards().get(0).getId().compareTo(co2.getCards().get(0).getId())).block();
        assertThat(list.get(0).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS4");
        assertThat(list.get(0).getCards().get(0).getHasBeenRead()).isFalse();
        assertThat(list.get(1).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS6");
        assertThat(list.get(1).getCards().get(0).getHasBeenRead()).isFalse();
        assertThat(list.get(2).getCards().get(0).getId()).isEqualTo("PROCESS.PROCESS7");
        assertThat(list.get(2).getCards().get(0).getHasBeenRead()).isTrue();

    }

    @NotNull
    private Predicate<CardConsultationData> computeCardPredicate(CardConsultationData card) {
        Predicate<CardConsultationData> predicate = c -> card.getId().equals(c.getId());
        predicate = predicate.and(c -> c.getDetails().size() == 1);
        predicate = predicate.and(c -> c.getDetails().get(0).getTitlePosition() == TitlePositionEnum.UP);
        predicate = predicate.and(c -> "PUBLISHER".equals(c.getPublisher()));
        predicate = predicate.and(c -> c.getEntityRecipients().size() == 2);
        predicate = predicate.and(c -> c.getEntityRecipients().get(0).equals("entity1"));
        predicate = predicate.and(c -> c.getEntityRecipients().get(1).equals("entity2"));
        return predicate;
    }


}

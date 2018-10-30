/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.lfenergy.operatorfabric.cards.consultation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.lfenergy.operatorfabric.cards.model.SeverityEnum;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * Please use builder to instantiate outside delinearization
 *
 * @author David Binder
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "archivedCards")
public class ArchivedConsultationCardData implements Card {

    @Id
    private String id;
    private String publisher;
    private String publisherVersion;
    private String processId;
    private I18n title;
    private I18n summary;
    @CreatedDate
    private Long publishDate;
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long deletionDate;
    private Long lttd;
    @Indexed
    private Long startDate;
    @Indexed
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long endDate;
    @JsonIgnore
    private String media;
    private SeverityEnum severity;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Singular
    private List<String> tags;
    @Transient
    @JsonIgnore
    @Singular
    private Map<String, ? extends Action> actions;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Singular
    private List<? extends Detail> details;
    @JsonIgnore
    private Recipient recipient;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
    @Indexed
    private int shardKey;
    private String mainRecipient;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Singular
    private List<String> userRecipients;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Singular
    private List<String> groupRecipients;


}

Feature: getDetailsThird

  Background:
   #Getting token for admin and tso1-operator user calling getToken.feature
    * def signIn = call read('../common/getToken.feature') { username: 'admin'}
    * def authToken = signIn.authToken
    * def signInAsTSO = call read('../common/getToken.feature') { username: 'tso1-operator'}
    * def authTokenAsTSO = signInAsTSO.authToken
    
    * def process = 'api_test'
    * def state = 'messageState'
    * def version = 2

    Scenario: get third details

      Given url opfabUrl + '/thirds/processes/' + process + '/' + state + '/details?version=' + version
      And header Authorization = 'Bearer ' + authToken
      When method get
      Then print response
      And status 200
      And print response.title.key



  Scenario: get third details without authentication

    Given url opfabUrl + '/thirds/processes/' + process + '/' + state + '/details?version=' + version
    When method get
    Then print response
    And status 401


  Scenario: get third details without authentication

    Given url opfabUrl + '/thirds/unknownThird/' + process + '/' + state + '/details?version=' + version
    And header Authorization = 'Bearer ' + authToken
    When method get
    Then print response
    And status 404

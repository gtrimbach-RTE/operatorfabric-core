//In this test we check the current language
//Then we make sur that all labels are visible and displayed in the correct language
//After that the language is changed through an API call
//Finaly we test that labels are consequently displayed in the new language 

describe ('Check_Menu_Translations',()=>{
    var lang
it('Check Language',()=>{
    cy.getLang().then(reponse =>
     {
         if(reponse == 'en')
         {            
             lang=reponse
             cy.log('The current language is '+lang)
         }
         else
         {
         lang=reponse    
         cy.log ('La langue choisie est '+ lang)
         }
        })
        })
it ('Check Menu Labels, change language then recheck',()=>{
    cy.LogOpFab('tso1-operator', 'test');//If I remove it the translation in french doesn't appear
    cy.checkMenus(lang)    
    cy.goToSettings()
    cy.changeLangManually(lang)
    cy.wait(2000)
    cy.getLang().then(response =>
    {        
            lang=response
            cy.log(lang)
            cy.checkMenus(lang)  
        })
})
})
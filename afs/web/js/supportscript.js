function hidePanel(panelname)
{
    Richfaces.hideModalPanel(panelname);
}
        
    
function showAdditionalExportSettings()
{
    var allElem = document.getElementsByName("FormTypeAddAttrForm:AddFormType:SelectExportMethod");
            
    var i=0;
    for(elem in allElem)
    {
        if(allElem[elem].checked)
        {
            if( allElem[elem].value == "No export")
            {
                document.getElementById("FormTypeAddAttrForm:exportFormatId").disabled=true;
                                                
                document.getElementById("FormTypeAddAttrForm:imageFormatId").disabled=true;
                document.getElementById("FormTypeAddAttrForm:fromMailId").disabled=true;
                document.getElementById("FormTypeAddAttrForm:toMailId").disabled=true;
            }
            else if(allElem[elem].value == "Mail export")
            {
                document.getElementById("FormTypeAddAttrForm:fromMailId:inputFromMailId").style.visibility='hidden';
                document.getElementById("FormTypeAddAttrForm:toMailId:inputToMailId").style.visibility='hidden';
                        
                document.getElementById("FormTypeAddAttrForm:exportFormatId:selectExportFormatId").style.visibility='visible';
                document.getElementById("FormTypeAddAttrForm:imageFormatId:selectImageFormatId").style.visibility='visible';
            }
            else
            {
                document.getElementById("FormTypeAddAttrForm:exportFormatId:selectExportFormatId").style.visibility='visible';
                                                
                document.getElementById("FormTypeAddAttrForm:imageFormatId:selectImageFormatId").style.visibility='visible';
                document.getElementById("FormTypeAddAttrForm:fromMailId:inputFromMailId").style.visibility='visible';
                document.getElementById("FormTypeAddAttrForm:toMailId:inputToMailId").style.visibility='visible';
                
                
            }
        }
    }
            
}
        
function showAdditionalExportSettings2()
{
    var allElem = document.getElementsByName("EditFormType:AddFormType:SelectExportMethod");
            
    var i=0;
    for(elem in allElem)
    {
        if(allElem[elem].checked)
        {
            if( allElem[elem].value == "No export")
            {
                document.getElementById("EditFormType:exportFormatId").style.visibility='hidden';
                                                
                document.getElementById("EditFormType:imageFormatId").style.visibility='hidden';
                document.getElementById("EditFormType:fromMailId").style.visibility='hidden';
                document.getElementById("EditFormType:toMailId").style.visibility='hidden';
            }
            else if(allElem[elem].value == "Mail export")
            {
                document.getElementById("EditFormTyp:fromMailId:inputFromMailId").style.visibility='hidden';
                document.getElementById("EditFormTyp:toMailId:inputToMailId").style.visibility='hidden';
                        
                document.getElementById("EditFormTyp:exportFormatId:selectExportFormatId").style.visibility='visible';
                document.getElementById("EditFormTyp:imageFormatId:selectImageFormatId").style.visibility='visible';
            }
            else
            {
                document.getElementById("EditFormTyp:exportFormatId:selectExportFormatId").style.visibility='visible';
                                                
                document.getElementById("EditFormTyp:imageFormatId:selectImageFormatId").style.visibility='visible';
                document.getElementById("EditFormTyp:fromMailId:inputFromMailId").style.visibility='visible';
                document.getElementById("EditFormTyp:toMailId:inputToMailId").style.visibility='visible';
                
                
            }
        }
    }
            
}

function setFocusOnUsername(){
   var elements = document.getElementById('myform').getElementsByTagName('input');
    
   var i = 0;
   while(i < elements.length){
      if(elements[i].name.endsWith('username')){
        elements[i].focus();
        
      }
      i++;
    }
  
}

function showConfirm(){
                
  elements = document.getElementsByName('span');
  show = true;
                
  var i = 0;
  while(i < elements.length){
    if(elements[i].className == 'errorText' && elements[i].innerHTML.length != 0){
      show = false;
    }
    i++;
  }
                
  if(show){
    Richfaces.showModalPanel('ConfirmFormTypePanel');
  }
}


function checkCookies()
{
  document.cookie = 'TestCookiesEnabled';
  
  var allCookies = document.cookie.split(';');
  
  for(i = 0; i < allCookies.length; i++ ){
    tmpCookies = allCookies[i].split('=');  
      
    if(tmpCookies[0] == 'TestCookiesEnabled'){
      return true;
    }
  }
  
  document.getElementById('noCookiesID').style.visibility='visible';
  
  return false;
}


function endisableAuth()
{
  setActivate("mc_auth_password","mc_authenticate_activate_checkbox");
  setActivate("mc_auth_user","mc_authenticate_activate_checkbox");
}


function endisablePop()
{
    setActivate("mc_pop_host","mc_pop_activate_checkbox");
    setActivate("mc_pop_port","mc_pop_activate_checkbox");
    setActivate("mc_pop_username","mc_pop_activate_checkbox");
    setActivate("mc_pop_password","mc_pop_activate_checkbox");
    var pop = document.getElementById('MailSettings:mc_pop_activate_checkbox');
    if(pop.checked)
    {
        document.getElementById('MailSettings:mc_imap_activate_checkbox').checked=false;
    }

}

function endisableImap()
{
    var imap = document.getElementById('MailSettings:mc_imap_activate_checkbox');
    if(imap.checked)
    {
        var pop = document.getElementById('MailSettings:mc_pop_activate_checkbox');
        if (pop.checked)
        {
            document.getElementById('MailSettings:mc_pop_activate_checkbox').checked=false;
            endisablePop();
        }
    }
}
                
function setActivate(vid,cid)
{
    var v = document.getElementById('MailSettings:'+vid);
    var c = document.getElementById('MailSettings:'+cid);
    v.disabled=!c.checked;
}
                
function onLoadImpl()
{
    endisablePop();
    endisableAuth();
}
package com.anoto.ash.web.actions.forms;

import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.database.FormTypeData;
import com.anoto.ash.database.UserData;
import com.anoto.ash.services.FormCopyService;
import com.anoto.ash.services.UserService;
import com.anoto.ash.utils.ResourceHandler;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;

@Name("completedAction")
@Scope(ScopeType.CONVERSATION)
@SuppressWarnings("unused")
public class CompletedAction
{

  @In
  FacesMessages facesMessages;

  @In(create=true)
  private FormCopyService formCopyService;

  @In(value="userService", create=true)
  UserService userService;

  @DataModel("completed")
  private List<FormCopyData> completed;

  @DataModelSelection
  @In(value="selectedCompletedFormCopy", required=false, scope=ScopeType.PAGE)
  @Out(value="selectedCompletedFormCopy", required=false, scope=ScopeType.PAGE)
  private FormCopyData selectedCompletedFormCopy;
  private int page;
  private String date;
  private String formTypeName;
  private String pageAddress;
  private String userName;
  private String penSerial;
  private String status;
  private Integer maxNumberOfPages;
  private boolean update;
  
  public CompletedAction()
  {
    this.page = 1;
    this.date = "";
    this.formTypeName = "";
    this.pageAddress = "";
    this.userName = "";
    this.penSerial = "";
    this.status = "";
    this.maxNumberOfPages = Integer.valueOf(0);
    this.update = true;
  }

  public void getCompletedFormCopies() {
    if (this.update) {
      FormCopyData fc = new FormCopyData();
      fc.setMarkedCompleted(1);
      fc.setVerificationNeeded(0);
      fc.setMandatoryFieldsMissing(0);
      fc.setOwner(getUserCriteria());

      if (this.date.length() > 0) {
        fc.setLatestSubmit(convertToDate(this.date));
      }

      if (this.formTypeName.length() > 0) {
        FormTypeData formType = new FormTypeData();
        formType.setFormTypeName(this.formTypeName + "%");

        fc.setFormType(formType);
      }

      if (this.pageAddress.length() > 0) {
        fc.setEndPageAddress(this.pageAddress + "%");
      }
      
      int sortOrder = 3;
      String orderProperty = "";

      this.completed = this.formCopyService.getCompletedCopies(fc, (this.page - 1) * 10, orderProperty, sortOrder);
      this.maxNumberOfPages = this.formCopyService.countNumberOfCompletedFormCopies(fc, (this.page - 1) * 10);

      if (this.maxNumberOfPages.intValue() % 10 == 0) {
        this.maxNumberOfPages = Integer.valueOf(this.maxNumberOfPages.intValue() / 10);
      }
      else {
        this.maxNumberOfPages = Integer.valueOf(this.maxNumberOfPages.intValue() / 10 + 1);
      }

      this.update = false;
    }
  }

  public Integer getMaxNumberOfPages()
  {
    return this.maxNumberOfPages;
  }

  public void setMaxNumberOfPages(Integer maxNumberOfPages) {
    this.maxNumberOfPages = maxNumberOfPages;
  }

  public int getPage()
  {
    return this.page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public void nextPage()
  {
    if (this.page < this.maxNumberOfPages.intValue()) {
      this.page += 1;
      this.update = true;
    }
  }

  public void previousPage() {
    if (this.page > 1) {
      this.page -= 1;
      this.update = true;
    }
  }

  public void lastPage() {
    this.page = this.maxNumberOfPages.intValue();
    this.update = true;
  }

  public void firstPage() {
    this.page = 1;
    this.update = true;
  }

  private UserData getUserCriteria()
  {
    if ((Identity.instance().hasRole("Admin")) || (Identity.instance().hasRole("Verifier")))
    {
      if ((getUserName().length() > 0) || (getPenSerial().length() > 0)) {
        UserData user = new UserData();

        if (getUserName().length() > 0) {
          user.setUserName(getUserName() + "%");
        }

        if (this.penSerial.length() > 0) {
          user.setPenId(this.penSerial + "%");
        }

        return user;
      }

      return null;
    }

    UserData user = new UserData();
    user.setUserName(Identity.instance().getUsername());
    user = (UserData)this.userService.getUser(user).get(0);

    return user;
  }

  public String editFormCopy()
  {
    String page = "";

    if (this.selectedCompletedFormCopy.getFormStatus() == 3)
    {
      page = "/forms/Completed.html";
    }

    return page;
  }

  public Date convertToDate(String dateInString)
  {
    String defaultDate = "1900-01-01 00:00";

    String newStr = defaultDate.substring(dateInString.length(), defaultDate.length());

    newStr = dateInString + newStr;

    Format formatter = new SimpleDateFormat(ResourceHandler.getResource("date_Format"));

    Date result = null;
    try
    {
      result = ((SimpleDateFormat)formatter).parse(newStr);
    }
    catch (Exception e) {
      try {
        result = ((SimpleDateFormat)formatter).parse(defaultDate);
      }
      catch (ParseException ex)
      {
      }
    }
    return result;
  }

  public String convertDate(Date date) {
    Format formatter = new SimpleDateFormat(ResourceHandler.getResource("date_Format"));

    return formatter.format(date);
  }

  public String getLockedStatus(FormCopyData formCopy)
  {
    if (formCopy.getLocked() == 1) {
      return "Locked.";
    }

    return "";
  }

  public String getDate()
  {
    return this.date;
  }

  public void setDate(String date)
  {
    if (!(this.date.equals(date))) {
      this.date = date;
      this.page = 1;
      this.update = true;
    }
  }

  public String getFormTypeName() {
    return this.formTypeName;
  }

  public void setFormTypeName(String formTypeName)
  {
    if (!(this.formTypeName.equals(formTypeName))) {
      this.formTypeName = formTypeName;
      this.page = 1;
      this.update = true;
    }
  }

  public String getPageAddress()
  {
    return this.pageAddress;
  }

  public void setPageAddress(String pageAddress)
  {
    if (!(this.pageAddress.equals(pageAddress))) {
      this.pageAddress = pageAddress;
      this.page = 1;
      this.update = true;
    }
  }

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(String userName)
  {
    if (!(this.userName.equals(userName))) {
      this.userName = userName;
      this.page = 1;
      this.update = true;
    }
  }

  public String getPenSerial() {
    return this.penSerial;
  }

  public void setPenSerial(String penSerial)
  {
    if (!(this.penSerial.equals(penSerial))) {
      this.penSerial = penSerial;
      this.page = 1;
      this.update = true;
    }
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status)
  {
    if (!(status.equals(status))) {
      this.status = status;
      this.page = 1;
      this.update = true;
    }
  }

  public void applyFilter() {
    this.update = true;
  }
  
}
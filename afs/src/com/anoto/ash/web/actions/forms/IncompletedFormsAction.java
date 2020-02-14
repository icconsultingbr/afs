package com.anoto.ash.web.actions.forms;

import com.anoto.api.util.PadFileUtility;
import com.anoto.ash.database.FormCopyData;
import com.anoto.ash.services.FormCopyService;
import com.anoto.ash.utils.ResourceHandler;
import java.net.MalformedURLException;
import java.util.List;
import javax.faces.context.FacesContext;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;

@Name("incompletedFormsAction")
@Scope(ScopeType.CONVERSATION)
@SuppressWarnings("unused")
public class IncompletedFormsAction extends FormCopyPageRenderable
{

  @In
  private FacesMessages facesMessages;

  @In
  private FacesContext facesContext;

  @In(create=true)
  private FormCopyService formCopyService;

  @In(value="incompletedFormCopy", required=false)
  @Out(value="incompletedFormCopy", required=false)
  private FormCopyData incompletedFormCopy;

  @RequestParameter("editFormCopyAddress")
  String editFormCopyAddress;
  private int page;
  private int totalNumberOfPages;

  public IncompletedFormsAction()
  {
    this.page = 0;
    this.totalNumberOfPages = 1;
  }

  public void editIncompletedForm() {
    if (this.incompletedFormCopy == null) {
      this.incompletedFormCopy = new FormCopyData();
      this.incompletedFormCopy.setEndPageAddress(this.editFormCopyAddress);

      this.incompletedFormCopy = ((FormCopyData)this.formCopyService.getFormCopies(this.incompletedFormCopy).get(0));
      this.totalNumberOfPages = PadFileUtility.getPageAddresses(this.incompletedFormCopy.getEndPageAddress(), "Anoto_Forms_Solution_ASH").length;
    }
  }

  public int getPage() {
    return this.page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getTotalNumberOfPages() {
    return this.totalNumberOfPages;
  }

  public void getNextPage() {
    this.page += 1;

    if (this.page >= this.totalNumberOfPages)
      this.page = (this.totalNumberOfPages - 1);
  }

  public void getPreviousPage()
  {
    this.page -= 1;

    if (this.page < 0)
      this.page = 0;
  }

  public void changeStatus()
  {
	/*
	 * Código comentado para impedir que a ficha seja fechada finalizada na visualização.

    if (this.formCopyService.changeToComplete(this.incompletedFormCopy)) {
      this.facesMessages.clear();
    }
    else {
      this.facesMessages.clear();
      //this.facesMessages.add(ResourceHandler.getResource("form_changed_to_complete_failed"), new Object[0]);
    }
    */
    this.page = 0;
  }

  public void cancel() {
    this.page = 0;
  }

  public String getCurrentImage() throws MalformedURLException {
    return getRenderedPage(this.incompletedFormCopy, this.page);
  }
}
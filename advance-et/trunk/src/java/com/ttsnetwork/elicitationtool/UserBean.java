package com.ttsnetwork.elicitationtool;

/**
 *
 * @author farago
 */
public class UserBean {
    private boolean allowToEdit;
    private boolean allowToView;
    private boolean allowToDownload;
    private boolean allowToUpload;
    private boolean allowToDelete;

    public void setAllowToDelete(boolean allowToDelete) {
        this.allowToDelete = allowToDelete;
    }

    public void setAllowToDownload(boolean allowToDownload) {
        this.allowToDownload = allowToDownload;
    }

    public void setAllowToEdit(boolean allowToEdit) {
        this.allowToEdit = allowToEdit;
    }

    public void setAllowToUpload(boolean allowToUpload) {
        this.allowToUpload = allowToUpload;
    }

    public void setAllowToView(boolean allowToView) {
        this.allowToView = allowToView;
    }

    public boolean isAllowToDelete() {
        return allowToDelete;
    }

    public boolean isAllowToDownload() {
        return allowToDownload;
    }

    public boolean isAllowToEdit() {
        return allowToEdit;
    }

    public boolean isAllowToUpload() {
        return allowToUpload;
    }

    public boolean isAllowToView() {
        return allowToView;
    }
}

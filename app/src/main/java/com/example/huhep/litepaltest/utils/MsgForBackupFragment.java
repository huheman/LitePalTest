package com.example.huhep.litepaltest.utils;

import com.example.huhep.litepaltest.fragments.BackupFragment;

public class MsgForBackupFragment {
    private boolean editTextCanEdit;
    private boolean btnIsEnable;
    private String btnMsg;
    private String hintTVMsg;

    public MsgForBackupFragment(boolean editTextCanEdit, boolean btnIsEnable, String hintTVMsg) {
        this(editTextCanEdit, btnIsEnable, null, hintTVMsg);
    }

    public MsgForBackupFragment(boolean editTextCanEdit, boolean btnIsEnable, String btnMsg, String hintTVMsg) {
        this.editTextCanEdit = editTextCanEdit;
        this.btnIsEnable = btnIsEnable;
        this.btnMsg = btnMsg;
        this.hintTVMsg = hintTVMsg;
    }

    public MsgForBackupFragment(String hintTVMsg) {
        this.editTextCanEdit = BackupFragment.currentState.editTextCanEdit;
        this.btnIsEnable = BackupFragment.currentState.btnIsEnable;
        this.btnMsg = BackupFragment.currentState.btnMsg;
        this.hintTVMsg = hintTVMsg;
    }

    public String getBtnMsg() {
        return btnMsg;
    }

    public boolean isEditTextCanEdit() {
        return editTextCanEdit;
    }

    public boolean isBtnIsEnable() {
        return btnIsEnable;
    }

    public String getHintTVMsg() {
        return hintTVMsg;
    }
}

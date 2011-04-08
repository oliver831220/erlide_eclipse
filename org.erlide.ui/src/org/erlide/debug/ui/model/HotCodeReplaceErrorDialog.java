/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.debug.ui.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.erlide.jinterface.ErlLogger;

/**
 * An error dialog reporting a problem with a debug target which gives the user
 * the option to continue or terminate/disconnect or restart the target.
 */
public class HotCodeReplaceErrorDialog extends ErrorDialog {

    protected IDebugTarget target;
    // The IDs of the buttons. Set to the sum of the other possible IDs
    // generated by
    // this dialog to ensure the IDs' uniqueness.
    protected int TERMINATE_ID = IDialogConstants.OK_ID
            + IDialogConstants.DETAILS_ID + IDialogConstants.CANCEL_ID;
    protected int DISCONNECT_ID = TERMINATE_ID + 1;
    protected int RESTART_ID = TERMINATE_ID + 2;

    /**
     * Creates a new dialog which can terminate, disconnect or restart the given
     * debug target.
     * 
     * @param target
     *            the debug target
     * @see ErrorDialogWithToggle#ErrorDialogWithToggle(Shell, String, String,
     *      IStatus, String, String, IPreferenceStore)
     */
    public HotCodeReplaceErrorDialog(final Shell parentShell,
            final String dialogTitle, final String message,
            final IStatus status, final IDebugTarget target) {
        super(parentShell, dialogTitle, message, status, IStatus.ERROR);
        this.target = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setText("&Continue");
        final boolean canTerminate = target.canTerminate();
        final boolean canDisconnect = target.canDisconnect();
        if (canTerminate) {
            createButton(parent, TERMINATE_ID, "&Terminate", false);
        }
        if (canDisconnect) {
            createButton(parent, DISCONNECT_ID, "Di&sconnect", false);
        }
        if (canTerminate && !canDisconnect) {
            createButton(parent, RESTART_ID, "&Restart", false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(final int id) {
        if (id == TERMINATE_ID || id == DISCONNECT_ID || id == RESTART_ID) {
            final String[] operation = new String[1];
            final Runnable r = new Runnable() {
                public void run() {
                    try {
                        if (id == TERMINATE_ID) {
                            operation[0] = "Terminate";
                            target.terminate();
                        } else if (id == DISCONNECT_ID) {
                            operation[0] = "Disconnect";
                            target.disconnect();
                        } else {
                            operation[0] = "Restart";
                            final ILaunch launch = target.getLaunch();
                            launch.terminate();
                            final ILaunchConfiguration config = launch
                                    .getLaunchConfiguration();
                            if (config != null && config.exists()) {
                                DebugUITools.launch(config,
                                        launch.getLaunchMode());
                            }
                        }
                    } catch (final DebugException e) {
                        ErlLogger.error(e);
                    }
                }
            };
            BusyIndicator.showWhile(getShell().getDisplay(), r);
            okPressed();
        } else {
            super.buttonPressed(id);
        }
    }
}

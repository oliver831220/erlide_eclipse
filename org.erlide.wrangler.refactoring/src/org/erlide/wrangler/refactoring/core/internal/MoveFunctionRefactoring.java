package org.erlide.wrangler.refactoring.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.erlide.wrangler.refactoring.backend.IRefactoringRpcMessage;
import org.erlide.wrangler.refactoring.backend.RefactoringState;
import org.erlide.wrangler.refactoring.backend.WranglerBackendManager;
import org.erlide.wrangler.refactoring.core.SimpleOneStepWranglerRefactoring;
import org.erlide.wrangler.refactoring.selection.IErlMemberSelection;
import org.erlide.wrangler.refactoring.selection.IErlSelection;
import org.erlide.wrangler.refactoring.selection.IErlSelection.SelectionKind;
import org.erlide.wrangler.refactoring.util.GlobalParameters;

import com.ericsson.otp.erlang.OtpErlangBoolean;

public class MoveFunctionRefactoring extends SimpleOneStepWranglerRefactoring {

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		IErlSelection sel = GlobalParameters.getWranglerSelection();
		if (sel instanceof IErlMemberSelection) {
			SelectionKind kind = sel.getKind();
			if (kind == SelectionKind.FUNCTION_CLAUSE
					|| kind == SelectionKind.FUNCTION)
				return new RefactoringStatus();
		}
		return RefactoringStatus
				.createFatalErrorStatus("Please select a function!");
	}

	@Override
	public String getName() {
		return "Move function";
	}

	@Override
	public IRefactoringRpcMessage run(IErlSelection selection) {
		IErlMemberSelection sel = (IErlMemberSelection) selection;
		return WranglerBackendManager.getRefactoringBackend().call(
				"move_fun_eclipse", "siisxxi", sel.getFilePath(),
				sel.getMemberRange().getStartLine(),
				sel.getMemberRange().getStartCol(), userInput,
				new OtpErlangBoolean(false), sel.getSearchPath(),
				GlobalParameters.getTabWidth());
	}

	public IRefactoringRpcMessage run2(IErlSelection selection) {
		IErlMemberSelection sel = (IErlMemberSelection) selection;
		return WranglerBackendManager.getRefactoringBackend().call(
				"move_fun_1_eclipse", "siisxxi", sel.getFilePath(),
				sel.getMemberRange().getStartLine(),
				sel.getMemberRange().getStartCol(), userInput,
				new OtpErlangBoolean(false), sel.getSearchPath(),
				GlobalParameters.getTabWidth());
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		IErlSelection sel = GlobalParameters.getWranglerSelection();
		IRefactoringRpcMessage message = run(sel);
		if (message.isSuccessful()) {
			changedFiles = message.getRefactoringChangeset();
			return new RefactoringStatus();
		} else if (message.getRefactoringState() == RefactoringState.QUESTION) {
			message = run2(sel);
			if (message.isSuccessful()) {
				changedFiles = message.getRefactoringChangeset();
				return new RefactoringStatus();
			} else {
				return RefactoringStatus.createFatalErrorStatus(message
						.getMessageString());
			}
		} else {
			return RefactoringStatus.createFatalErrorStatus(message
					.getMessageString());
		}
	}
}

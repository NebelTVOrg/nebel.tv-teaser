package com.nebel_tv.github;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.nebel_tv.R;
import com.nebel_tv.github.GitHubIssueAsyncTask.FeedbackType;
import com.nebel_tv.utils.UIUtils;

public class GitHubIssueDialogFragment extends DialogFragment {
	
	private static final String TAG = GitHubIssueDialogFragment.class.getName();
	
	public static void showGitHubIssueDialog(FragmentManager fm) {
		GitHubIssueDialogFragment gitHubIssueDialog = new GitHubIssueDialogFragment();		
		gitHubIssueDialog.show(fm, TAG);
	}
	
	private EditText emailInput;
	private Spinner feedbackTypeSpinner;
	private EditText feedbackTextInput;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        builder
        	.setTitle(R.string.feedback_title)
        	.setView(inflater.inflate(R.layout.feedback, null))
            .setPositiveButton(R.string.send_feedback, null);

        return builder.create();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		AlertDialog d = (AlertDialog) getDialog();
		
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String email = emailInput.getText().toString();
				FeedbackType feedbackType = FeedbackType.values()[feedbackTypeSpinner.getSelectedItemPosition()];
				String feedbackText = feedbackTextInput.getText().toString();
				
				if(feedbackText!=null && !"".equals(feedbackText)) {
					new GitHubIssueAsyncTask(
							getActivity(), feedbackText, email, feedbackType).execute();
					dismiss();
				} else {
					UIUtils.showMessage(R.string.feedback_text_error_msg);
				}
			}
		});
		
		emailInput = (EditText) d.findViewById(R.id.input_email);
		feedbackTextInput = (EditText) d.findViewById(R.id.input_feedback_text);
		feedbackTypeSpinner = (Spinner) d.findViewById(R.id.spinner_feedback_type);
		
		ArrayAdapter<String> adapter = 
			new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_spinner_item, FeedbackType.getFeedBackTypeTitles(getActivity()));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		feedbackTypeSpinner.setAdapter(adapter);
	}
}

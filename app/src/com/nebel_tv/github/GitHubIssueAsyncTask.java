package com.nebel_tv.github;

import java.io.IOException;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.nebel_tv.R;
import com.nebel_tv.utils.D;
import com.nebel_tv.utils.UIUtils;

public class GitHubIssueAsyncTask extends AsyncTask<Void, Void, Boolean> {
	
	public static enum FeedbackType {
				
		NONE(EMPTY_RES_VALUE),
		PRODUCT_FEEDBACK(R.string.product_feedback),
		FEATURE_PROPOSAL(R.string.feature_proposal),
		BUG_REPORT(R.string.bug_report),
		PROJECT_CONTRIBUTION(R.string.project_contribution),
		OTHER(R.string.other);
		
		private int resId;
		
		private FeedbackType(int resId) {
			this.resId = resId;
		}
		
		public int getResId() {
			return resId;
		}
		
		public static String[] getFeedBackTypeTitles(Context context) {
			FeedbackType[] items = FeedbackType.values();
			String[] titles = new String[items.length];
			for(int i=0; i<titles.length; i++) {
				int resId = items[i].getResId();
				titles[i] = resId!=EMPTY_RES_VALUE?context.getString(resId):"";
			}
			return titles;
		}
	}
	
	private static final int EMPTY_RES_VALUE = -1;
	private static final String REPO_ID = "NebelTVOrg/nebel.tv-teaser";
	private static final String DEFAULT_USER_LOGIN = "kowalski89";
	private static final String DEFAULT_USER_PASS = "udpbwt723";
	private static final int DEFAULT_TITLE_LENGTH = 10;
	
	private Context context;
	private String feedbackText;
	private String email;
	private FeedbackType feedbackType;
	
	private ProgressDialog progressDialog;
	
	public GitHubIssueAsyncTask(Context context, 
								String feedbackText, String email, FeedbackType feedbackType) {
		if(feedbackText==null) {
			throw new IllegalArgumentException("feedbackText must not be null");
		}
		this.context = context;
		this.feedbackText = feedbackText;
		this.email = email;
		this.feedbackType = feedbackType;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = UIUtils.createBorderlessProgressDialog(context);
		progressDialog.show();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		GitHubClient client = new GitHubClient();
		client.setCredentials(DEFAULT_USER_LOGIN, DEFAULT_USER_PASS);
		IssueService service = new IssueService(client);
		
		RepositoryId repo = RepositoryId.createFromId(REPO_ID);
		Issue issue = createIssue();
		try {
			service.createIssue(repo, issue);
			return true;
		} catch (IOException e) {
			D.e(e, false);
			return false;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		progressDialog.dismiss();
		UIUtils.showMessage(result?R.string.feedback_success_msg:R.string.feedback_failure_msg);
	}
	
	private Issue createIssue() {
		Issue issue = new Issue();
		StringBuilder bodyText = new StringBuilder();
		if(email!=null) {
			bodyText.append(String.format(context.getString(R.string.feedback_email_field), email));
			bodyText.append("\n");
		}
		if(feedbackType!=null && feedbackType!=FeedbackType.NONE) {
			bodyText.append(String.format(context.getString(R.string.feedback_type_field),
														context.getString(feedbackType.getResId())));
			bodyText.append("\n");
		}	
		bodyText.append(String.format(context.getString(R.string.feedback_text_field), feedbackText));
		
		issue.setBody(bodyText.toString());
		int titleLength = feedbackText.length()>DEFAULT_TITLE_LENGTH?DEFAULT_TITLE_LENGTH:feedbackText.length();
		issue.setTitle(feedbackText.substring(0, titleLength));
		
		return issue;
	}
	
}

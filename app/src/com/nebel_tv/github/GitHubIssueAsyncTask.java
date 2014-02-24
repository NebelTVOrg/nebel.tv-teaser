/**
 * Copyright (C) 2014 Nebel TV (http://nebel.tv)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

	public interface OnGitHubIssueCompletedListener {
		void onGitHubIssueCompleted();
	}

	public static enum FeedbackType {

		NONE(EMPTY_RES_VALUE), PRODUCT_FEEDBACK(R.string.product_feedback), FEATURE_PROPOSAL(R.string.feature_proposal), BUG_REPORT(
				R.string.bug_report), PROJECT_CONTRIBUTION(R.string.project_contribution), OTHER(R.string.other);

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
			for (int i = 0; i < titles.length; i++) {
				int resId = items[i].getResId();
				titles[i] = resId != EMPTY_RES_VALUE ? context.getString(resId) : "";
			}
			return titles;
		}
	}

	private static final int EMPTY_RES_VALUE = -1;
	private static final String REPO_ID = "NebelTVOrg/nebel.tv-teaser";
	private static final String DEFAULT_USER_LOGIN = "feedback-nebeltv ";
	private static final String DEFAULT_USER_PASS = "KUAL9cR9a6EJ";
	private static final int DEFAULT_TITLE_LENGTH = 10;

	private Context context;
	private String feedbackText;
	private String email;
	private FeedbackType feedbackType;
	private OnGitHubIssueCompletedListener listener;

	private ProgressDialog progressDialog;

	public GitHubIssueAsyncTask(Context context, OnGitHubIssueCompletedListener listener, String feedbackText, String email, FeedbackType feedbackType) {
		if (feedbackText == null) {
			throw new IllegalArgumentException("feedbackText must not be null");
		}
		this.context = context;
		this.listener = listener;
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
		UIUtils.showMessage(result ? R.string.feedback_success_msg : R.string.feedback_failure_msg);
		if (listener != null) {
			listener.onGitHubIssueCompleted();
		}

	}

	private Issue createIssue() {
		Issue issue = new Issue();
		StringBuilder bodyText = new StringBuilder();
		if (email != null && !email.isEmpty()) {
			bodyText.append(String.format(context.getString(R.string.feedback_email_msg_field), email));
			bodyText.append("\n");
		}
		if (feedbackType != null && feedbackType != FeedbackType.NONE) {
			bodyText.append(String.format(context.getString(R.string.feedback_type_msg_field), context.getString(feedbackType.getResId())));
			bodyText.append("\n");
		}
		bodyText.append(String.format(context.getString(R.string.feedback_msg_field), feedbackText));

		issue.setBody(bodyText.toString());
		int titleLength = feedbackText.length() > DEFAULT_TITLE_LENGTH ? DEFAULT_TITLE_LENGTH : feedbackText.length();
		issue.setTitle(feedbackText.substring(0, titleLength));

		return issue;
	}

}

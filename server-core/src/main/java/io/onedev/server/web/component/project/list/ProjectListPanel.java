package io.onedev.server.web.component.project.list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.OrEntityCriteria;
import io.onedev.server.search.entity.project.NameCriteria;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ProjectQueryBehavior;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.datatable.LoadableDetachableDataProvider;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.project.avatar.ProjectAvatar;
import io.onedev.server.web.component.savedquery.SavedQueriesClosed;
import io.onedev.server.web.component.savedquery.SavedQueriesOpened;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.util.QuerySaveSupport;

@SuppressWarnings("serial")
public class ProjectListPanel extends Panel {
	
	private final IModel<String> queryModel;
	
	private final int expectedCount;
	
	private final IModel<ProjectQuery> parsedQueryModel = new LoadableDetachableModel<ProjectQuery>() {

		@Override
		protected ProjectQuery load() {
			String query = queryModel.getObject();
			try {
				return ProjectQuery.parse(query);
			} catch (OneException e) {
				error(e.getMessage());
				return null;
			} catch (Exception e) {
				warn("Not a valid formal query, performing fuzzy query");
				List<EntityCriteria<Project>> criterias = new ArrayList<>();
				criterias.add(new NameCriteria("*" + query + "*"));
				return new ProjectQuery(new OrEntityCriteria<Project>(criterias));
			}
		}
		
	};
	
	private DataTable<Project, Void> dataTable;	
	
	public ProjectListPanel(String id, IModel<String> queryModel, int expectedCount) {
		super(id);
		this.queryModel = queryModel;
		this.expectedCount = expectedCount;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Override
	protected void onDetach() {
		queryModel.detach();
		parsedQueryModel.detach();
		super.onDetach();
	}
	
	@Nullable
	protected PagingHistorySupport getPagingHistorySupport() {
		return null;
	}
	
	@Nullable
	protected QuerySaveSupport getQuerySaveSupport() {
		return null;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (SecurityUtils.canCreateProjects()) 
			add(new BookmarkablePageLink<Void>("newProject", NewProjectPage.class));
		else
			add(new WebMarkupContainer("newProject").setVisible(false));
		
		add(new AjaxLink<Void>("showSavedQueries") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SavedQueriesClosed) 
					((SavedQueriesClosed) event.getPayload()).getHandler().add(this);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getQuerySaveSupport() != null && !getQuerySaveSupport().isSavedQueriesVisible());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				send(getPage(), Broadcast.BREADTH, new SavedQueriesOpened(target));
				target.add(this);
			}
			
		}.setOutputMarkupPlaceholderTag(true));

		Component saveQueryLink;
		add(saveQueryLink = new AjaxLink<Void>("saveQuery") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(parsedQueryModel.getObject() != null);
				setVisible(SecurityUtils.getUser() != null && getQuerySaveSupport() != null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled()) 
					tag.put("disabled", "disabled");
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getQuerySaveSupport().onSaveQuery(target, queryModel.getObject());
			}		
			
		});
		
		TextField<String> input = new TextField<String>("input", queryModel);
		input.add(new ProjectQueryBehavior());
		
		WebMarkupContainer body = new WebMarkupContainer("body");
		add(body.setOutputMarkupId(true));
		
		Form<?> form = new Form<Void>("query");
		form.add(input);
		form.add(new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				dataTable.setCurrentPage(0);
				target.add(body);
				if (SecurityUtils.getUser() != null && getQuerySaveSupport() != null)
					target.add(saveQueryLink);
			}
			
		});
		if (SecurityUtils.canCreateProjects())
			form.add(AttributeAppender.append("class", "can-create-projects"));
		add(form);
		
		SortableDataProvider<Project, Void> dataProvider = new LoadableDetachableDataProvider<Project, Void>() {

			@Override
			public Iterator<? extends Project> iterator(long first, long count) {
				try {
					return getProjectManager().query(parsedQueryModel.getObject(), (int)first, (int)count).iterator();
				} catch (OneException e) {
					error(e.getMessage());
					return new ArrayList<Project>().iterator();
				}
			}

			@Override
			public long calcSize() {
				ProjectQuery parsedQuery = parsedQueryModel.getObject();
				if (parsedQuery != null) {
					try {
						return getProjectManager().count(parsedQuery.getCriteria());
					} catch (OneException e) {
						error(e.getMessage());
					}
				}
				return 0;
			}

			@Override
			public IModel<Project> model(Project object) {
				Long projectId = object.getId();
				return new LoadableDetachableModel<Project>() {

					@Override
					protected Project load() {
						return getProjectManager().load(projectId);
					}
					
				};
			}
			
		};
		
		if (expectedCount != 0 && expectedCount != dataProvider.size())
			warn("Some projects might be hidden due to permission policy");
		
		body.add(new FencedFeedbackPanel("feedback", this));
		
		List<IColumn<Project, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Project")) {

			@Override
			public String getCssClass() {
				return "project";
			}

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, IModel<Project> rowModel) {
				Fragment fragment = new Fragment(componentId, "projectFrag", ProjectListPanel.this);
				Project project = rowModel.getObject();
				
				ActionablePageLink<Void> link = new ActionablePageLink<Void>("link", 
						ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(
								getPage().getClass(), getPage().getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(Project.class, redirectUrlAfterDelete);
					}
					
				};
				
				link.add(new ProjectAvatar("avatar", project));
				link.add(new Label("name", project.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		columns.add(new AbstractColumn<Project, Void>(Model.of("Last Update")) {

			@Override
			public void populateItem(Item<ICellPopulator<Project>> cellItem, String componentId, 
					IModel<Project> rowModel) {
				Project project = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(project.getUpdateDate())));
			}

			@Override
			public String getCssClass() {
				return "last-update expanded";
			}
			
		});
		
		body.add(dataTable = new DefaultDataTable<Project, Void>("projects", columns, dataProvider, 
				WebConstants.PAGE_SIZE, getPagingHistorySupport()));
		
		setOutputMarkupId(true);
	}
		
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectListResourceReference()));
	}
	
}

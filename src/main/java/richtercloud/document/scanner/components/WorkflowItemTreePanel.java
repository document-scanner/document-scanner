/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.components;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.gui.EntityEditingDialog;
import richtercloud.document.scanner.ifaces.DocumentAddException;
import richtercloud.document.scanner.ifaces.MainPanel;
import richtercloud.document.scanner.model.WorkflowItem;
import richtercloud.document.scanner.model.validator.WorkflowItemValidationException;
import richtercloud.document.scanner.model.validator.WorkflowItemValidator;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorage;
import richtercloud.reflection.form.builder.jpa.panels.QueryListPanel;
import richtercloud.reflection.form.builder.jpa.storage.FieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.panels.ListPanelItemEvent;
import richtercloud.reflection.form.builder.panels.ListPanelItemEventVetoException;
import richtercloud.reflection.form.builder.panels.ListPanelItemListener;
import richtercloud.validation.tools.FieldRetrievalException;
import richtercloud.validation.tools.FieldRetriever;

/**
 * Allows to set in-reply-to relationship of {@link CommunicationItem} with
 * selection in a {@link QueryPanel} and visualizes this relationship in a
 * read-only communication tree.
 *
 * Since having one root node is enforced by {@link DefaultTreeModel} there's a
 * node labeling the tree as communcation tree and no extra label around the
 * panel.
 *
 * There's no point in providing basic information or all information of a
 * {@link WorkflowItem} in a special tree renderer components (e.g. a panel with
 * a {@link JTextArea} to providing the transciption of a phone call) because it
 * just doubles the efforts. It's fine if information summary is provided in
 * tree node label. Another option would be {@code JXTree} of SwingX project,
 * but it's unmaintained and hard to figure out how to use.
 *
 * @author richter
 */
/*
internal implementation notes:
- Having GroupLayout still makes sense with one component because it handles
gaps well and is easy to extend.
*/
public class WorkflowItemTreePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JTree communicationTree = new JTree();
    private final MutableTreeNode communicationTreeRoot = new DefaultMutableTreeNode("Communication tree:", true);
    private final DefaultTreeModel communicationTreeModel = new DefaultTreeModel(communicationTreeRoot,
            true);
    private final QueryListPanel<WorkflowItem> queryListPanel;
    private final Set<WorkflowItemTreePanelUpdateListener> updateListeners = new HashSet<>();
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final JScrollPane communicationTreeScrollPane;
    private final JScrollPane queryListPanelScrollPane;
    private final PersistenceStorage storage;
    private final IssueHandler issueHandler;
    private final ConfirmMessageHandler confirmMessageHandler;
    private final Set<Class<?>> entityClasses;
    private final Class<?> primaryClassSelection;
    private final MainPanel mainPanel;
    private final IdApplier idApplier;
    private final Map<Class<?>, WarningHandler<?>> warningHandlers;
    private final FieldInitializer fieldInitializer;
    private final QueryHistoryEntryStorage entryStorage;
    private final FieldRetriever fieldRetriever;

    /**
     *
     * @param entityManager
     * @param initialValue
     * @param issueHandler
     * @param reflectionFormBuilder
     * @param entityClasses
     * @param primaryClassSelection
     * @param mainPanel the {@link MainPanel} to add the documents for editing
     * to (when clicking on a communcation item)
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public WorkflowItemTreePanel(PersistenceStorage storage,
            List<WorkflowItem> initialValue,
            IssueHandler issueHandler,
            ConfirmMessageHandler confirmMessageHandler,
            FieldRetriever fieldRetriever,
            Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            MainPanel mainPanel,
            IdApplier idApplier,
            Map<Class<?>, WarningHandler<?>> warningHandlers,
            FieldInitializer fieldInitializer,
            QueryHistoryEntryStorage entryStorage) throws IllegalArgumentException, IllegalAccessException, FieldRetrievalException {
        this.entityClasses = entityClasses;
        this.primaryClassSelection = primaryClassSelection;
        this.storage = storage;
        this.issueHandler = issueHandler;
        this.confirmMessageHandler = confirmMessageHandler;
        this.mainPanel = mainPanel;
        if(idApplier == null) {
            throw new IllegalArgumentException("idApplier mustn't be null");
        }
        this.idApplier = idApplier;
        this.warningHandlers = warningHandlers;
        this.fieldInitializer = fieldInitializer;
        this.entryStorage = entryStorage;
        this.fieldRetriever = fieldRetriever;
        this.queryListPanel = new QueryListPanel<>(storage,
                fieldRetriever,
                WorkflowItem.class,
                issueHandler,
                initialValue,
                DocumentScanner.generateApplicationWindowTitle("Bidirectional relation help",
                        Constants.APP_NAME,
                        Constants.APP_VERSION),
                fieldInitializer,
                entryStorage);
        communicationTree.setModel(communicationTreeModel);

        initTreeModel(initialValue);
        communicationTree.setCellRenderer(new DefaultTreeCellRenderer() {
            private static final long serialVersionUID = 1L;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            EntityEditingDialog entityEditingDialog = new EntityEditingDialog(SwingUtilities.getWindowAncestor(WorkflowItemTreePanel.this), //parent
                                    WorkflowItemTreePanel.this.entityClasses,
                                    WorkflowItemTreePanel.this.primaryClassSelection,
                                    WorkflowItemTreePanel.this.storage,
                                    WorkflowItemTreePanel.this.issueHandler,
                                    WorkflowItemTreePanel.this.confirmMessageHandler,
                                    WorkflowItemTreePanel.this.idApplier,
                                    WorkflowItemTreePanel.this.warningHandlers,
                                    WorkflowItemTreePanel.this.fieldInitializer,
                                    WorkflowItemTreePanel.this.entryStorage,
                                    WorkflowItemTreePanel.this.fieldRetriever
                            );
                            entityEditingDialog.setVisible(true);
                            List<Object> selectedEntities = entityEditingDialog.getSelectedEntities();
                            for(Object selectedEntity : selectedEntities) {
                                try {
                                    WorkflowItemTreePanel.this.mainPanel.addDocument(selectedEntity);
                                } catch (DocumentAddException | IOException ex) {
                                    WorkflowItemTreePanel.this.issueHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                                }
                            }
                        }
                    }
                });
            }
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            }
        });

        communicationTreeScrollPane = new JScrollPane(communicationTree);
        queryListPanelScrollPane = new JScrollPane(queryListPanel);
        splitPane.setLeftComponent(communicationTreeScrollPane);
        splitPane.setRightComponent(queryListPanelScrollPane);
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup();
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        horizontalGroup.addComponent(splitPane,
                0,
                GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE);
        verticalGroup.addComponent(splitPane,
                0,
                GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);

        queryListPanel.addItemListener(new ListPanelItemListener<WorkflowItem>() {
            @Override
            public void onItemAdded(ListPanelItemEvent<WorkflowItem> event) throws ListPanelItemEventVetoException {
                for(WorkflowItemTreePanelUpdateListener updateListener : updateListeners) {
                    updateListener.onUpdate(new WorkflowItemTreePanelUpdateEvent(event.getItem(),
                            WorkflowItemTreePanel.this.queryListPanel.getBidirectionalControlPanel().getMappedField()));
                }
                //check whether a loop has been specified
                for(WorkflowItem selectedEntity : event.getItem()) {
                    try {
                        WorkflowItemValidator.validate(selectedEntity);
                    } catch (WorkflowItemValidationException ex) {
                        throw new ListPanelItemEventVetoException(ex);
                    }
                }
                initTreeModel(new LinkedList<>(event.getItem()));
            }

            @Override
            public void onItemRemoved(ListPanelItemEvent<WorkflowItem> event) {
                for(WorkflowItemTreePanelUpdateListener updateListener : updateListeners) {
                    updateListener.onUpdate(new WorkflowItemTreePanelUpdateEvent(queryListPanel.getSelectedEntities(),
                            WorkflowItemTreePanel.this.queryListPanel.getBidirectionalControlPanel().getMappedField()));
                }
                //assume that removing items from selection doesn't cause
                //invalid reference chains
                initTreeModel(new LinkedList<>(event.getItem()));
            }
        });
        this.communicationTreeModel.setRoot(communicationTreeRoot);
    }

    public void addUpdateListener(WorkflowItemTreePanelUpdateListener updateListener) {
        this.updateListeners.add(updateListener);
    }

    public void removeUpdateListener(WorkflowItemTreePanelUpdateListener updateListener) {
        this.updateListeners.remove(updateListener);
    }

    public void reset() throws FieldRetrievalException {
        this.queryListPanel.reset();
    }

    private void initTreeModel(List<WorkflowItem> items) {
        if(items == null) {
            throw new IllegalArgumentException("items mustn't be null");
        }
        List<MutableTreeNode> communicationTreeRootChildren = Collections.list(communicationTreeRoot.children());
        for(MutableTreeNode communicationTreeRootChild: communicationTreeRootChildren) {
            communicationTreeModel.removeNodeFromParent(communicationTreeRootChild);
        }
        //create tree nodes (assume that changes on entities aren't reflected in
        //database and thus other results retrieved in the same run of the
        //application)
        Set<WorkflowItem> roots = new HashSet<>();
        Queue<WorkflowItem> itemQueue = new LinkedList<>(items);
        while(!itemQueue.isEmpty()) {
            WorkflowItem head = itemQueue.poll();
            if(head.getPreviousItems().isEmpty()) {
                roots.add(head);
            }
            itemQueue.addAll(head.getPreviousItems());
        }
        for(WorkflowItem root : roots) {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root, true);
            communicationTreeModel.insertNodeInto(rootNode, communicationTreeRoot, 0);
            Queue<DefaultMutableTreeNode> rootQueue = new LinkedList<>(Arrays.asList(rootNode));
            while(!rootQueue.isEmpty()) {
                DefaultMutableTreeNode head = rootQueue.poll();
                for(WorkflowItem headFollowing : ((WorkflowItem)head.getUserObject()).getFollowingItems()) {
                    DefaultMutableTreeNode headFollowingNode = new DefaultMutableTreeNode(headFollowing, true);
                    communicationTreeModel.insertNodeInto(head, headFollowingNode, 0);
                    rootQueue.add(headFollowingNode);
                }
            }
        }

        //Implementation for WorkflowItem without two-way reference
//        Map<WorkflowItem, List<WorkflowItem>> inReplyToReversed = new HashMap<>();
//        for(WorkflowItem item : items) {
//            for(WorkflowItem inReplyTo : item.getPreviousItems()) {
//                List<WorkflowItem> inReplyToReversedItems = inReplyToReversed.get(inReplyTo);
//                if(inReplyToReversedItems == null) {
//                    inReplyToReversedItems = new LinkedList<>();
//                    inReplyToReversed.put(inReplyTo, inReplyToReversedItems);
//                }
//                inReplyToReversedItems.add(item);
//            }
//        }
//        Map<WorkflowItem, MutableTreeNode> itemNodeMap = new HashMap<>();
//        Queue<MutableTreeNode> nodeQueue = new LinkedList<>();
//        for(WorkflowItem item : inReplyToReversed.keySet()) {
//            MutableTreeNode itemNode = itemNodeMap.get(item);
//            if(itemNode == null) {
//                itemNode = new DefaultMutableTreeNode(item, true);
//                itemNodeMap.put(item, itemNode);
//            }
//            for(WorkflowItem itemChild : inReplyToReversed.get(item)) {
//                MutableTreeNode itemChildNode = itemNodeMap.get(itemChild);
//                if(itemChildNode == null) {
//                    itemChildNode = new DefaultMutableTreeNode(itemChild, true);
//                    itemNodeMap.put(itemChild, itemChildNode);
//                }
//                communicationTreeModel.insertNodeInto(itemChildNode, itemNode, 0);
//                    //DefaultMutableTreeNode.insert doesn't work
//            }
//            nodeQueue.add(itemNode);
//        }
//        if(!itemNodeMap.isEmpty()) {
//            //only add if there's any correspondance at all (otherwise
//            //NullPointerException occurs)
//            for(WorkflowItem item : items) {
//                MutableTreeNode itemNode = itemNodeMap.get(item);
//                if(itemNode.getParent() == null) {
//                    communicationTreeModel.insertNodeInto(itemNode, communicationTreeRoot, 0);
//                }
//            }
//        }
    }
}

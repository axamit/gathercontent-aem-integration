/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;


import com.axamit.gc.api.dto.GCFolder;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.core.exception.GCException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * Servlet return JSON of GatherContent items like a Tree structure.
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"tree"},
        extensions = {"json"},
        methods = {HttpConstants.METHOD_POST, HttpConstants.METHOD_GET}
)
public class GCItemsJSONTreeServlet extends GCAbstractServlet {

    private static final String ROOT_ITEM_TEXT = "root";

    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        try {
            final Integer projectId = NumberUtils.toInt(request.getRequestPathInfo().getSelectors()[1], 0);
            final List<GCItem> items = gcContentNewApi.itemsByProjectId(getGCContext(request), projectId);
//            final GCFolder foldersTreeByProjectId = api.foldersTreeByProjectId(getGCContext(request), projectId);

            Map<Integer, List<TreeNode>> parentChild = new HashMap<>();

            for (GCItem gcItem : items) {
//                putItemToFolderTree(foldersTreeByProjectId, gcItem);


//
//                gcItem.getFolderUuid()
//                int parentId = Integer.parseInt(gcItem.getParentId());
//                if (parentChild.containsKey(parentId)) {
//                    TreeNode node = new TreeNode(gcItem);
//                    parentChild.get(parentId).add(node);
//                } else {
//                    parentChild.put(Integer.valueOf(gcItem.getParentId()), new LinkedList<TreeNode>());
//                    parentChild.get(parentId).add(new TreeNode(gcItem));
//                }
            }
//            TreeNode root = new TreeNode();
//            root.setId(NumberUtils.INTEGER_ZERO);
//            root.setText(ROOT_ITEM_TEXT);
//
//            setChildrenToNode(root, parentChild);

//            String jsonString = JSONUtil.fromObjectToJsonString(root.getChildren());
//            String jsonString = JSONUtil.fromObjectToJsonString(foldersTreeByProjectId);

//            response.getWriter().print(jsonString);
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    private boolean putItemToFolderTree(final GCFolder gcFolder, final GCItem gcItem) {
        if (gcItem.getFolderUuid().equals(gcFolder.getUuid())) {
            if (gcFolder.getItems() == null) {
                gcFolder.setItems(new ArrayList<>(Arrays.asList(gcItem)));
            } else {
                gcFolder.getItems().add(gcItem);
            }
            return true;
        } else {
            final List<GCFolder> children = gcFolder.getFolders();
            if (children != null && !children.isEmpty()) {
                for (GCFolder child : children) {
                    if (putItemToFolderTree(child, gcItem)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setChildrenToNode(final TreeNode node, final Map<Integer, List<TreeNode>> parentChild) {
        if (!parentChild.keySet().isEmpty() && parentChild.containsKey(node.getId())) {
            node.setChildren(parentChild.get(node.getId()));
            parentChild.keySet().remove(node.getId());
            if (!node.getChildren().isEmpty()) {
                node.setLeaf(false);
            }
            for (TreeNode childOfChild : node.getChildren()) {
                setChildrenToNode(childOfChild, parentChild);
            }

        }
    }

    /**
     * Class <code>TreeNode</code> to represent GCItems like a Tree structure.
     */
    private static class TreeNode {
        private String name;
        private int id;
        private String text;
        private List<TreeNode> children;
        private boolean leaf = true;

        TreeNode() {
            children = new LinkedList<>();
        }

        TreeNode(final GCItem gc) {
            id = gc.getId();
            text = gc.getName();
            name = gc.getName();
        }

        public int getId() {
            return id;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(final String text) {
            this.text = text;
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        void setChildren(final List<TreeNode> children) {
            this.children = children;
        }

        public boolean isLeaf() {
            return leaf;
        }

        void setLeaf(final boolean leaf) {
            this.leaf = leaf;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

}


/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;


import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.JSONUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
            GCContentApi api = getGcContentApi();
            String projectId = request.getRequestPathInfo().getSelectors()[1];
            List<GCItem> items = api.itemsByProjectId(getGCContext(request), projectId);

            Map<Integer, List<TreeNode>> parentChild = new HashMap<>();

            for (GCItem gcItem : items) {
                int parentId = Integer.parseInt(gcItem.getParentId());
                if (parentChild.containsKey(parentId)) {
                    TreeNode node = new TreeNode(gcItem);
                    parentChild.get(parentId).add(node);
                } else {
                    parentChild.put(Integer.valueOf(gcItem.getParentId()), new LinkedList<TreeNode>());
                    parentChild.get(parentId).add(new TreeNode(gcItem));
                }
            }
            TreeNode root = new TreeNode();
            root.setId(NumberUtils.INTEGER_ZERO);
            root.setText(ROOT_ITEM_TEXT);

            setChildrenToNode(root, parentChild);

            String jsonString = JSONUtil.fromObjectToJsonString(root.getChildren());

            response.getWriter().print(jsonString);
        } catch (GCException e) {
            getLOGGER().error(e.getMessage(), e);
        }

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
            id = Integer.parseInt(gc.getId());
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


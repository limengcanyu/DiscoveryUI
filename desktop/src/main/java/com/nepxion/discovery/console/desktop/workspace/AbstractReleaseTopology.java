package com.nepxion.discovery.console.desktop.workspace;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nepxion.cots.twaver.element.TElementManager;
import com.nepxion.cots.twaver.element.TLink;
import com.nepxion.cots.twaver.element.TNode;
import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.console.controller.ConsoleController;
import com.nepxion.discovery.console.desktop.common.icon.ConsoleIconFactory;
import com.nepxion.discovery.console.desktop.common.locale.ConsoleLocaleFactory;
import com.nepxion.discovery.console.desktop.common.util.ComboBoxUtil;
import com.nepxion.discovery.console.desktop.workspace.panel.CreatePanel;
import com.nepxion.discovery.console.desktop.workspace.topology.NodeImageType;
import com.nepxion.discovery.console.desktop.workspace.topology.NodeLocation;
import com.nepxion.discovery.console.desktop.workspace.topology.NodeSizeType;
import com.nepxion.discovery.console.desktop.workspace.topology.NodeUI;
import com.nepxion.discovery.console.desktop.workspace.type.ConfigType;
import com.nepxion.discovery.console.desktop.workspace.type.DeployType;
import com.nepxion.discovery.console.desktop.workspace.type.NodeType;
import com.nepxion.discovery.console.desktop.workspace.type.ReleaseType;
import com.nepxion.discovery.console.desktop.workspace.type.StrategyType;
import com.nepxion.discovery.console.entity.Instance;
import com.nepxion.swing.action.JSecurityAction;
import com.nepxion.swing.button.ButtonManager;
import com.nepxion.swing.combobox.JBasicComboBox;
import com.nepxion.swing.dialog.JExceptionDialog;
import com.nepxion.swing.element.ElementNode;
import com.nepxion.swing.handle.HandleManager;
import com.nepxion.swing.icon.IconFactory;
import com.nepxion.swing.locale.SwingLocale;
import com.nepxion.swing.optionpane.JBasicOptionPane;
import com.nepxion.swing.selector.checkbox.JCheckBoxSelector;

public abstract class AbstractReleaseTopology extends AbstractTopology {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractReleaseTopology.class);

    protected NodeLocation nodeLocation = new NodeLocation(440, 100, 200, 0);
    protected NodeUI gatewayBlackNodeUI = new NodeUI(NodeImageType.GATEWAY_BLACK, NodeSizeType.LARGE, true);

    protected JBasicComboBox serviceIdComboBox;

    protected TNode gatewayNode;

    protected DeployType deployType;

    protected Instance gateway;
    protected Object[] serviceIds;

    public AbstractReleaseTopology(ReleaseType releaseType) {
        super(releaseType);
    }

    public void initializeData(String group, Instance gateway, StrategyType strategyType, ConfigType configType, DeployType deployType) {
        this.group = group;
        this.gateway = gateway;
        this.strategyType = strategyType;
        this.configType = configType;
        this.deployType = deployType;

        refreshData();
    }

    public void refreshData() {
        try {
            if (deployType == DeployType.DOMAIN_GATEWAY) {
                this.serviceIds = ConsoleController.getInstanceMap(Arrays.asList(group)).keySet().toArray();
            } else {
                this.serviceIds = ConsoleController.getServices().toArray();
            }
        } catch (Exception e) {
            JExceptionDialog.traceException(HandleManager.getFrame(this), ConsoleLocaleFactory.getString("operation_failure"), e);
        }
    }

    public List<Instance> getInstances(String serviceId) {
        if (StringUtils.isBlank(serviceId)) {
            return null;
        }

        try {
            return ConsoleController.getInstanceList(serviceId);
        } catch (Exception e) {
            JExceptionDialog.traceException(HandleManager.getFrame(this), ConsoleLocaleFactory.getString("operation_failure"), e);
        }

        return null;
    }

    public void initializeUI() {
        setTitle();
        setGatewayNode();

        refreshUI();
    }

    public void refreshUI() {
        setServiceUI();
        setMetadataUI();
    }

    public void setTitle() {
        background.setTitle(releaseType.getDescription() + " | " + strategyType.getDescription() + " | " + configType.getDescription() + " | " + deployType.getDescription());
    }

    public void setGatewayNode() {
        dataBox.clear();

        reset();

        gatewayNode = addNode(ButtonManager.getHtmlText(gateway.getServiceId() + "\n" + group), gatewayBlackNodeUI);
        gatewayNode.setUserObject(gateway);
        gatewayNode.setBusinessObject(NodeType.PORTAL);

        setNodeTopBottom(gatewayNode, false);
    }

    @SuppressWarnings("unchecked")
    public void setServiceUI() {
        serviceIdComboBox.setModel(new DefaultComboBoxModel<>(serviceIds));
    }

    public void setMetadataUI() {
        List<String> metadatas = new ArrayList<String>();
        String serviceId = ComboBoxUtil.getSelectedValue(serviceIdComboBox);
        if (StringUtils.isNotBlank(serviceId)) {
            List<Instance> instances = getInstances(serviceId);
            if (CollectionUtils.isNotEmpty(instances)) {
                for (Instance instance : instances) {
                    String metadata = instance.getMetadata().get(strategyType.toString());
                    if (StringUtils.isNotBlank(metadata)) {
                        metadatas.add(metadata);
                    }
                }
            }
        }
        if (!metadatas.contains(DiscoveryConstant.DEFAULT)) {
            metadatas.add(DiscoveryConstant.DEFAULT);
        }

        setMetadataUI(metadatas.toArray());
    }

    @SuppressWarnings("unchecked")
    public boolean hasNodes(String serviceId) {
        List<TNode> nodes = TElementManager.getNodes(dataBox);
        for (TNode node : nodes) {
            Instance instance = (Instance) node.getUserObject();
            if (StringUtils.equalsIgnoreCase(instance.getServiceId(), serviceId)) {
                return true;
            }
        }

        return false;
    }

    public TNode addNode(String name, NodeUI topologyEntity) {
        TNode node = createNode(name, topologyEntity, nodeLocation, 0);

        dataBox.addElement(node);

        return node;
    }

    @SuppressWarnings("unchecked")
    public TLink addLink(TNode fromNode, TNode toNode, Color linkFlowingColor) {
        List<TLink> links = TElementManager.getLinks(dataBox);
        for (TLink link : links) {
            if (link.getFrom() == fromNode && link.getTo() == toNode) {
                return null;
            }
        }

        TLink link = createLink(fromNode, toNode, linkFlowingColor != null);
        if (linkFlowingColor != null) {
            link.putLinkToArrowColor(Color.yellow);
            link.putLinkFlowing(true);
            link.putLinkFlowingColor(linkFlowingColor);
            link.putLinkFlowingWidth(3);
        }

        dataBox.addElement(link);

        return link;
    }

    @Override
    public void create() {
        CreatePanel createPanel = new CreatePanel();
        createPanel.setPreferredSize(new Dimension(480, 180));

        int selectedOption = JBasicOptionPane.showOptionDialog(HandleManager.getFrame(AbstractReleaseTopology.this), createPanel, ConsoleLocaleFactory.getString("create_tooltip") + " [ " + releaseType.getDescription() + " ]", JBasicOptionPane.DEFAULT_OPTION, JBasicOptionPane.PLAIN_MESSAGE, ConsoleIconFactory.getSwingIcon("banner/net.png"), new Object[] { SwingLocale.getString("confirm"), SwingLocale.getString("cancel") }, null, true);
        if (selectedOption != 0) {
            return;
        }

        StrategyType strategyType = createPanel.getStrategyType();
        ConfigType configType = createPanel.getConfigType();
        DeployType deployType = createPanel.getDeployType();

        String group = createPanel.getGroup();
        if (StringUtils.isBlank(group)) {
            JBasicOptionPane.showMessageDialog(HandleManager.getFrame(AbstractReleaseTopology.this), ConsoleLocaleFactory.getString("group_not_null"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

            return;
        }

        String gatewayId = null;
        if (configType == ConfigType.PARTIAL) {
            gatewayId = createPanel.getGatewayId();
            if (StringUtils.isBlank(gatewayId)) {
                JBasicOptionPane.showMessageDialog(HandleManager.getFrame(AbstractReleaseTopology.this), ConsoleLocaleFactory.getString("service_id_not_null"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                return;
            }
        }

        Instance gateway = new Instance();
        gateway.setServiceId(gatewayId != null ? gatewayId : ConsoleLocaleFactory.getString("portal_service_text"));
        Map<String, String> metadataMap = new HashMap<String, String>();
        gateway.setMetadata(metadataMap);

        initializeData(group, gateway, strategyType, configType, deployType);
        initializeUI();
    }

    @Override
    public String getServiceId() {
        if (configType == ConfigType.PARTIAL) {
            return gateway.getServiceId();
        } else {
            return group;
        }
    }

    @Override
    public String getRemoveTooltip() {
        return ConsoleLocaleFactory.getString("remove_service_strategy_tooltip");
    }

    @Override
    public void save(String config) {
        String group = getGroup();
        String serviceId = getServiceId();

        LOG.info("Save Config, group={}, serviceId={}, config=\n{}", group, serviceId, config);
    }

    @Override
    public void clear() {
        dataBox.clear();
        dataBox.addElement(gatewayNode);

        reset();
    }

    public JSecurityAction createRefreshServiceListAction() {
        JSecurityAction action = new JSecurityAction(ConsoleIconFactory.getSwingIcon("refresh.png"), ConsoleLocaleFactory.getString("refresh_service_list_tooltip")) {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                if (strategyType == null) {
                    return;
                }

                refreshData();
                refreshUI();
            }
        };

        return action;
    }

    public JSecurityAction createMetadataSelectorAction(JBasicComboBox metadataComboBox) {
        JSecurityAction action = new JSecurityAction(ConsoleIconFactory.getSwingIcon("direction_south.png"), ConsoleLocaleFactory.getString("metadata_selector_tooltip")) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings({ "unchecked", "rawtypes" })
            public void execute(ActionEvent e) {
                ComboBoxModel metadataComboBoxModel = metadataComboBox.getModel();
                List<ElementNode> metadataElementNodes = new ArrayList<ElementNode>();
                for (int i = 0; i < metadataComboBoxModel.getSize(); i++) {
                    String metadata = metadataComboBoxModel.getElementAt(i).toString();
                    metadataElementNodes.add(new ElementNode(metadata, IconFactory.getSwingIcon("component/file_chooser_16.png"), metadata, metadata));
                }

                JCheckBoxSelector checkBoxSelector = new JCheckBoxSelector(HandleManager.getFrame(AbstractReleaseTopology.this), ConsoleLocaleFactory.getString("metadata_selector_tooltip"), new Dimension(400, 350), metadataElementNodes);
                checkBoxSelector.setVisible(true);
                checkBoxSelector.dispose();

                if (!checkBoxSelector.isConfirmed()) {
                    return;
                }

                List<String> selectedMetadatas = checkBoxSelector.getSelectedUserObjects();
                if (CollectionUtils.isEmpty(selectedMetadatas)) {
                    return;
                }

                StringBuilder StringBuilder = new StringBuilder();
                int index = 0;
                for (String selectedMetadata : selectedMetadatas) {
                    StringBuilder.append(selectedMetadata);
                    if (index < selectedMetadatas.size() - 1) {
                        StringBuilder.append(DiscoveryConstant.SEPARATE);
                    }

                    index++;
                }

                metadataComboBox.setSelectedItem(StringBuilder.toString());
            }
        };

        return action;
    }

    public JSecurityAction createAddServiceStrategyAction() {
        JSecurityAction action = new JSecurityAction(ConsoleLocaleFactory.getString("add_text"), ConsoleIconFactory.getSwingIcon("interface.png"), ConsoleLocaleFactory.getString("add_service_strategy_tooltip")) {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                if (strategyType == null) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(AbstractReleaseTopology.this), ConsoleLocaleFactory.getString("portal_not_null"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                String serviceId = ComboBoxUtil.getSelectedValue(serviceIdComboBox);
                if (StringUtils.isBlank(serviceId)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(AbstractReleaseTopology.this), ConsoleLocaleFactory.getString("service_id_not_null"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                if (hasNodes(serviceId)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(AbstractReleaseTopology.this), serviceId + " " + ConsoleLocaleFactory.getString("existed"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                addServiceStrategy(serviceId);
            }
        };

        return action;
    }

    public JSecurityAction createModifyServiceStrategyAction() {
        JSecurityAction action = new JSecurityAction(ConsoleLocaleFactory.getString("modify_text"), ConsoleIconFactory.getSwingIcon("modify.png"), ConsoleLocaleFactory.getString("modify_service_strategy_tooltip")) {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                if (strategyType == null) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(AbstractReleaseTopology.this), ConsoleLocaleFactory.getString("portal_not_null"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                String serviceId = ComboBoxUtil.getSelectedValue(serviceIdComboBox);
                if (StringUtils.isBlank(serviceId)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(AbstractReleaseTopology.this), ConsoleLocaleFactory.getString("service_id_not_null"), SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                modifyServiceStrategy(serviceId);
            }
        };

        return action;
    }

    public abstract void reset();

    public abstract void setMetadataUI(Object[] metadatas);

    public abstract void addServiceStrategy(String serviceId);

    public abstract void modifyServiceStrategy(String serviceId);
}
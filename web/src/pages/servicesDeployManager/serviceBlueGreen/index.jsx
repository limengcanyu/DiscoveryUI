import { Button, Result, Space, Radio, Modal, Divider, Row, Col, Typography, Tabs, Input, Select } from 'antd';
import React, { useState } from 'react';
import { connect } from 'umi';
import { PlusOutlined, MinusOutlined, SettingOutlined, CaretRightOutlined, EditOutlined } from '@ant-design/icons';
import { constant } from 'lodash';
const { TabPane } = Tabs;

import ServiceBlueGreenPreview from './preview/index.jsx';
import ServiceBlueGreenSave from './save/index.jsx';
import ServiceBlueGreenNew from './add/index.jsx';
import ServiceBlueGreenSettingCondition from './settingCondition/index.jsx';
import ServiceBlueGreenSettingArrange from './settingArrange/index.jsx';
import ServiceBlueGreenSettingArgs from './settingArgs/index.jsx';

const serviceBlueGreen = (props) => {

  // 当前页面的全局配置,在各个组件间传递
  const { serviceBlueGreen = {} } = props
  const { gobal = {} } = serviceBlueGreen

  const handleNew = (values) => {
    const { dispatch } = props;
    dispatch({
      type: 'serviceBlueGreen/new',
      payload: { ...values },
    });
  }

  return (
    <>
      <Row>
        <Col flex={10}>
          <ServiceBlueGreenNew new={handleNew}></ServiceBlueGreenNew>
          <ServiceBlueGreenPreview></ServiceBlueGreenPreview>
          <ServiceBlueGreenSave></ServiceBlueGreenSave>
        </Col>
        <Col flex="450px">
          <Divider orientation="left">蓝绿条件</Divider>
          <Tabs defaultActiveKey="1" >
            <TabPane tab="蓝" key="1">
              <ServiceBlueGreenSettingCondition></ServiceBlueGreenSettingCondition>
            </TabPane>
            <TabPane tab="绿" key="2">
              <ServiceBlueGreenSettingCondition></ServiceBlueGreenSettingCondition>
            </TabPane>
          </Tabs>
          <Divider orientation="left">蓝绿编排</Divider>
          <ServiceBlueGreenSettingArrange gobal={gobal}></ServiceBlueGreenSettingArrange>
          <Divider orientation="left">蓝绿参数</Divider>
          <ServiceBlueGreenSettingArgs></ServiceBlueGreenSettingArgs>
        </Col>
      </Row>
    </>
  );
};

export default connect(({ serviceBlueGreen, loading }) => ({
  serviceBlueGreen
}))(serviceBlueGreen);

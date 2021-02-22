import { constant } from 'lodash';
import { Select, Space, Row, Col, Button, Input, Modal, Divider } from 'antd';
import React, { useState, useEffect } from 'react';

import { RedoOutlined, EditOutlined, DownOutlined } from '@ant-design/icons';

import { instanceMap } from '@/services/console'

const settingArrange = (props) => {

  const gobal = props.gobal;

  const [instanceList, setInstanceList] = useState([])

  const [visible, setVisible] = useState(false);
  const addSubmit = () => {
    setVisible(false);
  }

  const initInstanceList = (instanceMap) => {
    let list = []
    for (var instanceName in instanceMap) {
      list.push({
        name: instanceName,
        value: instanceMap[instanceName]
      })
    }
    setInstanceList(list)
  }

  useEffect(() => {
    if (gobal.subscribe) {
      instanceMap([gobal.subscribe]).then(initInstanceList)
    }
  }, [gobal])

  return (
    <>
      <Space direction="vertical" style={{ width: "100%" }}>
        <Row gutter={{ xs: 1, sm: 2, md: 3 }}>
          <Col flex="40px">服务</Col>
          <Col flex="310px">
            <Select
              style={{ width: "100%" }}
              showSearch>
                {
                instanceList.map(item => {
                  return (
                    <Option value={item.name}>{item.name}</Option>
                  )
                })
              }
            </Select>
          </Col>
          <Col flex={1}>
            <Button type="primary" shape="circle" icon={<RedoOutlined />} />
          </Col>
        </Row>
        <Row gutter={{ xs: 1, sm: 2, md: 3 }}>
          <Col flex="40px">蓝</Col>
          <Col flex="310px">
            <Select
              style={{ width: "100%" }}
              showSearch>
            </Select>
          </Col>
        </Row>
        <Row gutter={{ xs: 1, sm: 2, md: 3 }}>
          <Col flex="40px">绿</Col>
          <Col flex="310px">
            <Select
              style={{ width: "100%" }}
              showSearch>
            </Select>
          </Col>
        </Row>
        <Row gutter={{ xs: 1, sm: 2, md: 3 }}>
          <Col flex="40px">兜底</Col>
          <Col flex="310px">
            <Select
              style={{ width: "100%" }}
              showSearch>
            </Select>
          </Col>
        </Row>
        <Row>
          <Col>
            <Space>
              <Button type="primary" shape="round" icon={<EditOutlined />} >
                添加
                  </Button>
              <Button type="primary" shape="round" icon={<EditOutlined />} >
                修改
                  </Button>
            </Space>

          </Col>
        </Row>
      </Space>
      <Modal visible={visible}
        onOk={addSubmit}
        onCancel={() => {
          setVisible(false);
        }}
      >
        <Divider orientation="left">元数据拾取器</Divider>

      </Modal>
    </>
  )
}

export default settingArrange;
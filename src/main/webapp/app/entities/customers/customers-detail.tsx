import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './customers.reducer';

export const CustomersDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const customersEntity = useAppSelector(state => state.customers.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="customersDetailsHeading">
          <Translate contentKey="hmPharmacyApp.customers.detail.title">Customers</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{customersEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="hmPharmacyApp.customers.name">Name</Translate>
            </span>
          </dt>
          <dd>{customersEntity.name}</dd>
          <dt>
            <span id="phone">
              <Translate contentKey="hmPharmacyApp.customers.phone">Phone</Translate>
            </span>
          </dt>
          <dd>{customersEntity.phone}</dd>
          <dt>
            <span id="email">
              <Translate contentKey="hmPharmacyApp.customers.email">Email</Translate>
            </span>
          </dt>
          <dd>{customersEntity.email}</dd>
          <dt>
            <span id="address">
              <Translate contentKey="hmPharmacyApp.customers.address">Address</Translate>
            </span>
          </dt>
          <dd>{customersEntity.address}</dd>
        </dl>
        <Button tag={Link} to="/customers" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/customers/${customersEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default CustomersDetail;

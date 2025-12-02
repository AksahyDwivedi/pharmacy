import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './suppliers.reducer';

export const SuppliersDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const suppliersEntity = useAppSelector(state => state.suppliers.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="suppliersDetailsHeading">
          <Translate contentKey="hmPharmacyApp.suppliers.detail.title">Suppliers</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{suppliersEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="hmPharmacyApp.suppliers.name">Name</Translate>
            </span>
          </dt>
          <dd>{suppliersEntity.name}</dd>
          <dt>
            <span id="contactPerson">
              <Translate contentKey="hmPharmacyApp.suppliers.contactPerson">Contact Person</Translate>
            </span>
          </dt>
          <dd>{suppliersEntity.contactPerson}</dd>
          <dt>
            <span id="phone">
              <Translate contentKey="hmPharmacyApp.suppliers.phone">Phone</Translate>
            </span>
          </dt>
          <dd>{suppliersEntity.phone}</dd>
          <dt>
            <span id="email">
              <Translate contentKey="hmPharmacyApp.suppliers.email">Email</Translate>
            </span>
          </dt>
          <dd>{suppliersEntity.email}</dd>
          <dt>
            <span id="address">
              <Translate contentKey="hmPharmacyApp.suppliers.address">Address</Translate>
            </span>
          </dt>
          <dd>{suppliersEntity.address}</dd>
        </dl>
        <Button tag={Link} to="/suppliers" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/suppliers/${suppliersEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SuppliersDetail;

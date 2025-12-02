import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './medicines.reducer';

export const MedicinesDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const medicinesEntity = useAppSelector(state => state.medicines.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="medicinesDetailsHeading">
          <Translate contentKey="hmPharmacyApp.medicines.detail.title">Medicines</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{medicinesEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="hmPharmacyApp.medicines.name">Name</Translate>
            </span>
          </dt>
          <dd>{medicinesEntity.name}</dd>
          <dt>
            <span id="manufacturer">
              <Translate contentKey="hmPharmacyApp.medicines.manufacturer">Manufacturer</Translate>
            </span>
          </dt>
          <dd>{medicinesEntity.manufacturer}</dd>
          <dt>
            <span id="category">
              <Translate contentKey="hmPharmacyApp.medicines.category">Category</Translate>
            </span>
          </dt>
          <dd>{medicinesEntity.category}</dd>
          <dt>
            <span id="price">
              <Translate contentKey="hmPharmacyApp.medicines.price">Price</Translate>
            </span>
          </dt>
          <dd>{medicinesEntity.price}</dd>
          <dt>
            <span id="stock">
              <Translate contentKey="hmPharmacyApp.medicines.stock">Stock</Translate>
            </span>
          </dt>
          <dd>{medicinesEntity.stock}</dd>
        </dl>
        <Button tag={Link} to="/medicines" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/medicines/${medicinesEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default MedicinesDetail;

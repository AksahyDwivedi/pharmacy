import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './purchase-items.reducer';

export const PurchaseItemsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const purchaseItemsEntity = useAppSelector(state => state.purchaseItems.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="purchaseItemsDetailsHeading">
          <Translate contentKey="hmPharmacyApp.purchaseItems.detail.title">PurchaseItems</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{purchaseItemsEntity.id}</dd>
          <dt>
            <span id="quantity">
              <Translate contentKey="hmPharmacyApp.purchaseItems.quantity">Quantity</Translate>
            </span>
          </dt>
          <dd>{purchaseItemsEntity.quantity}</dd>
          <dt>
            <span id="price">
              <Translate contentKey="hmPharmacyApp.purchaseItems.price">Price</Translate>
            </span>
          </dt>
          <dd>{purchaseItemsEntity.price}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.purchaseItems.purchases">Purchases</Translate>
          </dt>
          <dd>{purchaseItemsEntity.purchases ? purchaseItemsEntity.purchases.id : ''}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.purchaseItems.medicines">Medicines</Translate>
          </dt>
          <dd>{purchaseItemsEntity.medicines ? purchaseItemsEntity.medicines.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/purchase-items" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/purchase-items/${purchaseItemsEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PurchaseItemsDetail;

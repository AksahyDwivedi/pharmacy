import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './sale-items.reducer';

export const SaleItemsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const saleItemsEntity = useAppSelector(state => state.saleItems.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="saleItemsDetailsHeading">
          <Translate contentKey="hmPharmacyApp.saleItems.detail.title">SaleItems</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{saleItemsEntity.id}</dd>
          <dt>
            <span id="quantity">
              <Translate contentKey="hmPharmacyApp.saleItems.quantity">Quantity</Translate>
            </span>
          </dt>
          <dd>{saleItemsEntity.quantity}</dd>
          <dt>
            <span id="price">
              <Translate contentKey="hmPharmacyApp.saleItems.price">Price</Translate>
            </span>
          </dt>
          <dd>{saleItemsEntity.price}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.saleItems.medicines">Medicines</Translate>
          </dt>
          <dd>{saleItemsEntity.medicines ? saleItemsEntity.medicines.id : ''}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.saleItems.sales">Sales</Translate>
          </dt>
          <dd>{saleItemsEntity.sales ? saleItemsEntity.sales.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/sale-items" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/sale-items/${saleItemsEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SaleItemsDetail;

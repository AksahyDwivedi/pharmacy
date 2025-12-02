import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './sales.reducer';

export const SalesDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const salesEntity = useAppSelector(state => state.sales.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="salesDetailsHeading">
          <Translate contentKey="hmPharmacyApp.sales.detail.title">Sales</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{salesEntity.id}</dd>
          <dt>
            <span id="saleDate">
              <Translate contentKey="hmPharmacyApp.sales.saleDate">Sale Date</Translate>
            </span>
          </dt>
          <dd>{salesEntity.saleDate ? <TextFormat value={salesEntity.saleDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="invoiceNumber">
              <Translate contentKey="hmPharmacyApp.sales.invoiceNumber">Invoice Number</Translate>
            </span>
          </dt>
          <dd>{salesEntity.invoiceNumber}</dd>
          <dt>
            <span id="totalAmount">
              <Translate contentKey="hmPharmacyApp.sales.totalAmount">Total Amount</Translate>
            </span>
          </dt>
          <dd>{salesEntity.totalAmount}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.sales.customers">Customers</Translate>
          </dt>
          <dd>{salesEntity.customers ? salesEntity.customers.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/sales" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/sales/${salesEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SalesDetail;

import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './payments.reducer';

export const PaymentsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const paymentsEntity = useAppSelector(state => state.payments.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="paymentsDetailsHeading">
          <Translate contentKey="hmPharmacyApp.payments.detail.title">Payments</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{paymentsEntity.id}</dd>
          <dt>
            <span id="paymentDate">
              <Translate contentKey="hmPharmacyApp.payments.paymentDate">Payment Date</Translate>
            </span>
          </dt>
          <dd>
            {paymentsEntity.paymentDate ? <TextFormat value={paymentsEntity.paymentDate} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="paymentMethod">
              <Translate contentKey="hmPharmacyApp.payments.paymentMethod">Payment Method</Translate>
            </span>
          </dt>
          <dd>{paymentsEntity.paymentMethod}</dd>
          <dt>
            <span id="paymentStatus">
              <Translate contentKey="hmPharmacyApp.payments.paymentStatus">Payment Status</Translate>
            </span>
          </dt>
          <dd>{paymentsEntity.paymentStatus}</dd>
          <dt>
            <span id="amount">
              <Translate contentKey="hmPharmacyApp.payments.amount">Amount</Translate>
            </span>
          </dt>
          <dd>{paymentsEntity.amount}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.payments.sales">Sales</Translate>
          </dt>
          <dd>{paymentsEntity.sales ? paymentsEntity.sales.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/payments" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/payments/${paymentsEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PaymentsDetail;

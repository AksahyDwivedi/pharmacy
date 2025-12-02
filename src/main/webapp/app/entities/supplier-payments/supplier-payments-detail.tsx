import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './supplier-payments.reducer';

export const SupplierPaymentsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const supplierPaymentsEntity = useAppSelector(state => state.supplierPayments.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="supplierPaymentsDetailsHeading">
          <Translate contentKey="hmPharmacyApp.supplierPayments.detail.title">SupplierPayments</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{supplierPaymentsEntity.id}</dd>
          <dt>
            <span id="paymentDate">
              <Translate contentKey="hmPharmacyApp.supplierPayments.paymentDate">Payment Date</Translate>
            </span>
          </dt>
          <dd>
            {supplierPaymentsEntity.paymentDate ? (
              <TextFormat value={supplierPaymentsEntity.paymentDate} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="paymentMethod">
              <Translate contentKey="hmPharmacyApp.supplierPayments.paymentMethod">Payment Method</Translate>
            </span>
          </dt>
          <dd>{supplierPaymentsEntity.paymentMethod}</dd>
          <dt>
            <span id="paymentStatus">
              <Translate contentKey="hmPharmacyApp.supplierPayments.paymentStatus">Payment Status</Translate>
            </span>
          </dt>
          <dd>{supplierPaymentsEntity.paymentStatus}</dd>
          <dt>
            <span id="amountPaid">
              <Translate contentKey="hmPharmacyApp.supplierPayments.amountPaid">Amount Paid</Translate>
            </span>
          </dt>
          <dd>{supplierPaymentsEntity.amountPaid}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.supplierPayments.suppliers">Suppliers</Translate>
          </dt>
          <dd>{supplierPaymentsEntity.suppliers ? supplierPaymentsEntity.suppliers.id : ''}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.supplierPayments.purchases">Purchases</Translate>
          </dt>
          <dd>{supplierPaymentsEntity.purchases ? supplierPaymentsEntity.purchases.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/supplier-payments" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/supplier-payments/${supplierPaymentsEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SupplierPaymentsDetail;

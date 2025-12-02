import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getSales } from 'app/entities/sales/sales.reducer';
import { createEntity, getEntity, reset, updateEntity } from './payments.reducer';

export const PaymentsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const sales = useAppSelector(state => state.sales.entities);
  const paymentsEntity = useAppSelector(state => state.payments.entity);
  const loading = useAppSelector(state => state.payments.loading);
  const updating = useAppSelector(state => state.payments.updating);
  const updateSuccess = useAppSelector(state => state.payments.updateSuccess);

  const handleClose = () => {
    navigate('/payments');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getSales({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    values.paymentDate = convertDateTimeToServer(values.paymentDate);
    if (values.amount !== undefined && typeof values.amount !== 'number') {
      values.amount = Number(values.amount);
    }

    const entity = {
      ...paymentsEntity,
      ...values,
      sales: sales.find(it => it.id.toString() === values.sales?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          paymentDate: displayDefaultDateTime(),
        }
      : {
          ...paymentsEntity,
          paymentDate: convertDateTimeFromServer(paymentsEntity.paymentDate),
          sales: paymentsEntity?.sales?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.payments.home.createOrEditLabel" data-cy="PaymentsCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.payments.home.createOrEditLabel">Create or edit a Payments</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="payments-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.payments.paymentDate')}
                id="payments-paymentDate"
                name="paymentDate"
                data-cy="paymentDate"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.payments.paymentMethod')}
                id="payments-paymentMethod"
                name="paymentMethod"
                data-cy="paymentMethod"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.payments.paymentStatus')}
                id="payments-paymentStatus"
                name="paymentStatus"
                data-cy="paymentStatus"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.payments.amount')}
                id="payments-amount"
                name="amount"
                data-cy="amount"
                type="text"
              />
              <ValidatedField
                id="payments-sales"
                name="sales"
                data-cy="sales"
                label={translate('hmPharmacyApp.payments.sales')}
                type="select"
              >
                <option value="" key="0" />
                {sales
                  ? sales.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/payments" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default PaymentsUpdate;

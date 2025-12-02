import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getSuppliers } from 'app/entities/suppliers/suppliers.reducer';
import { getEntities as getPurchases } from 'app/entities/purchases/purchases.reducer';
import { createEntity, getEntity, reset, updateEntity } from './supplier-payments.reducer';

export const SupplierPaymentsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const suppliers = useAppSelector(state => state.suppliers.entities);
  const purchases = useAppSelector(state => state.purchases.entities);
  const supplierPaymentsEntity = useAppSelector(state => state.supplierPayments.entity);
  const loading = useAppSelector(state => state.supplierPayments.loading);
  const updating = useAppSelector(state => state.supplierPayments.updating);
  const updateSuccess = useAppSelector(state => state.supplierPayments.updateSuccess);

  const handleClose = () => {
    navigate('/supplier-payments');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getSuppliers({}));
    dispatch(getPurchases({}));
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
    if (values.amountPaid !== undefined && typeof values.amountPaid !== 'number') {
      values.amountPaid = Number(values.amountPaid);
    }

    const entity = {
      ...supplierPaymentsEntity,
      ...values,
      suppliers: suppliers.find(it => it.id.toString() === values.suppliers?.toString()),
      purchases: purchases.find(it => it.id.toString() === values.purchases?.toString()),
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
          ...supplierPaymentsEntity,
          paymentDate: convertDateTimeFromServer(supplierPaymentsEntity.paymentDate),
          suppliers: supplierPaymentsEntity?.suppliers?.id,
          purchases: supplierPaymentsEntity?.purchases?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.supplierPayments.home.createOrEditLabel" data-cy="SupplierPaymentsCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.supplierPayments.home.createOrEditLabel">Create or edit a SupplierPayments</Translate>
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
                  id="supplier-payments-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.supplierPayments.paymentDate')}
                id="supplier-payments-paymentDate"
                name="paymentDate"
                data-cy="paymentDate"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.supplierPayments.paymentMethod')}
                id="supplier-payments-paymentMethod"
                name="paymentMethod"
                data-cy="paymentMethod"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.supplierPayments.paymentStatus')}
                id="supplier-payments-paymentStatus"
                name="paymentStatus"
                data-cy="paymentStatus"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.supplierPayments.amountPaid')}
                id="supplier-payments-amountPaid"
                name="amountPaid"
                data-cy="amountPaid"
                type="text"
              />
              <ValidatedField
                id="supplier-payments-suppliers"
                name="suppliers"
                data-cy="suppliers"
                label={translate('hmPharmacyApp.supplierPayments.suppliers')}
                type="select"
              >
                <option value="" key="0" />
                {suppliers
                  ? suppliers.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="supplier-payments-purchases"
                name="purchases"
                data-cy="purchases"
                label={translate('hmPharmacyApp.supplierPayments.purchases')}
                type="select"
              >
                <option value="" key="0" />
                {purchases
                  ? purchases.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/supplier-payments" replace color="info">
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

export default SupplierPaymentsUpdate;

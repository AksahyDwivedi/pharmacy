import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getSuppliers } from 'app/entities/suppliers/suppliers.reducer';
import { createEntity, getEntity, reset, updateEntity } from './purchases.reducer';

export const PurchasesUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const suppliers = useAppSelector(state => state.suppliers.entities);
  const purchasesEntity = useAppSelector(state => state.purchases.entity);
  const loading = useAppSelector(state => state.purchases.loading);
  const updating = useAppSelector(state => state.purchases.updating);
  const updateSuccess = useAppSelector(state => state.purchases.updateSuccess);

  const handleClose = () => {
    navigate('/purchases');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getSuppliers({}));
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
    if (values.totalAmount !== undefined && typeof values.totalAmount !== 'number') {
      values.totalAmount = Number(values.totalAmount);
    }

    const entity = {
      ...purchasesEntity,
      ...values,
      suppliers: suppliers.find(it => it.id.toString() === values.suppliers?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...purchasesEntity,
          suppliers: purchasesEntity?.suppliers?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.purchases.home.createOrEditLabel" data-cy="PurchasesCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.purchases.home.createOrEditLabel">Create or edit a Purchases</Translate>
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
                  id="purchases-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.purchases.purchaseDate')}
                id="purchases-purchaseDate"
                name="purchaseDate"
                data-cy="purchaseDate"
                type="date"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.purchases.invoiceNumber')}
                id="purchases-invoiceNumber"
                name="invoiceNumber"
                data-cy="invoiceNumber"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.purchases.totalAmount')}
                id="purchases-totalAmount"
                name="totalAmount"
                data-cy="totalAmount"
                type="text"
              />
              <ValidatedField
                id="purchases-suppliers"
                name="suppliers"
                data-cy="suppliers"
                label={translate('hmPharmacyApp.purchases.suppliers')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/purchases" replace color="info">
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

export default PurchasesUpdate;

import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getPurchases } from 'app/entities/purchases/purchases.reducer';
import { getEntities as getMedicines } from 'app/entities/medicines/medicines.reducer';
import { createEntity, getEntity, reset, updateEntity } from './purchase-items.reducer';

export const PurchaseItemsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const purchases = useAppSelector(state => state.purchases.entities);
  const medicines = useAppSelector(state => state.medicines.entities);
  const purchaseItemsEntity = useAppSelector(state => state.purchaseItems.entity);
  const loading = useAppSelector(state => state.purchaseItems.loading);
  const updating = useAppSelector(state => state.purchaseItems.updating);
  const updateSuccess = useAppSelector(state => state.purchaseItems.updateSuccess);

  const handleClose = () => {
    navigate('/purchase-items');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPurchases({}));
    dispatch(getMedicines({}));
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
    if (values.quantity !== undefined && typeof values.quantity !== 'number') {
      values.quantity = Number(values.quantity);
    }
    if (values.price !== undefined && typeof values.price !== 'number') {
      values.price = Number(values.price);
    }

    const entity = {
      ...purchaseItemsEntity,
      ...values,
      purchases: purchases.find(it => it.id.toString() === values.purchases?.toString()),
      medicines: medicines.find(it => it.id.toString() === values.medicines?.toString()),
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
          ...purchaseItemsEntity,
          purchases: purchaseItemsEntity?.purchases?.id,
          medicines: purchaseItemsEntity?.medicines?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.purchaseItems.home.createOrEditLabel" data-cy="PurchaseItemsCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.purchaseItems.home.createOrEditLabel">Create or edit a PurchaseItems</Translate>
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
                  id="purchase-items-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.purchaseItems.quantity')}
                id="purchase-items-quantity"
                name="quantity"
                data-cy="quantity"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.purchaseItems.price')}
                id="purchase-items-price"
                name="price"
                data-cy="price"
                type="text"
              />
              <ValidatedField
                id="purchase-items-purchases"
                name="purchases"
                data-cy="purchases"
                label={translate('hmPharmacyApp.purchaseItems.purchases')}
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
              <ValidatedField
                id="purchase-items-medicines"
                name="medicines"
                data-cy="medicines"
                label={translate('hmPharmacyApp.purchaseItems.medicines')}
                type="select"
              >
                <option value="" key="0" />
                {medicines
                  ? medicines.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/purchase-items" replace color="info">
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

export default PurchaseItemsUpdate;

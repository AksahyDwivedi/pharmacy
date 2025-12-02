import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getMedicines } from 'app/entities/medicines/medicines.reducer';
import { getEntities as getSales } from 'app/entities/sales/sales.reducer';
import { createEntity, getEntity, reset, updateEntity } from './sale-items.reducer';

export const SaleItemsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const medicines = useAppSelector(state => state.medicines.entities);
  const sales = useAppSelector(state => state.sales.entities);
  const saleItemsEntity = useAppSelector(state => state.saleItems.entity);
  const loading = useAppSelector(state => state.saleItems.loading);
  const updating = useAppSelector(state => state.saleItems.updating);
  const updateSuccess = useAppSelector(state => state.saleItems.updateSuccess);

  const handleClose = () => {
    navigate('/sale-items');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getMedicines({}));
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
    if (values.quantity !== undefined && typeof values.quantity !== 'number') {
      values.quantity = Number(values.quantity);
    }
    if (values.price !== undefined && typeof values.price !== 'number') {
      values.price = Number(values.price);
    }

    const entity = {
      ...saleItemsEntity,
      ...values,
      medicines: medicines.find(it => it.id.toString() === values.medicines?.toString()),
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
      ? {}
      : {
          ...saleItemsEntity,
          medicines: saleItemsEntity?.medicines?.id,
          sales: saleItemsEntity?.sales?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.saleItems.home.createOrEditLabel" data-cy="SaleItemsCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.saleItems.home.createOrEditLabel">Create or edit a SaleItems</Translate>
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
                  id="sale-items-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.saleItems.quantity')}
                id="sale-items-quantity"
                name="quantity"
                data-cy="quantity"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.saleItems.price')}
                id="sale-items-price"
                name="price"
                data-cy="price"
                type="text"
              />
              <ValidatedField
                id="sale-items-medicines"
                name="medicines"
                data-cy="medicines"
                label={translate('hmPharmacyApp.saleItems.medicines')}
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
              <ValidatedField
                id="sale-items-sales"
                name="sales"
                data-cy="sales"
                label={translate('hmPharmacyApp.saleItems.sales')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/sale-items" replace color="info">
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

export default SaleItemsUpdate;

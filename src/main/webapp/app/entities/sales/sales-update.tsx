import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getCustomers } from 'app/entities/customers/customers.reducer';
import { createEntity, getEntity, reset, updateEntity } from './sales.reducer';

export const SalesUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const customers = useAppSelector(state => state.customers.entities);
  const salesEntity = useAppSelector(state => state.sales.entity);
  const loading = useAppSelector(state => state.sales.loading);
  const updating = useAppSelector(state => state.sales.updating);
  const updateSuccess = useAppSelector(state => state.sales.updateSuccess);

  const handleClose = () => {
    navigate('/sales');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getCustomers({}));
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
    values.saleDate = convertDateTimeToServer(values.saleDate);
    if (values.totalAmount !== undefined && typeof values.totalAmount !== 'number') {
      values.totalAmount = Number(values.totalAmount);
    }

    const entity = {
      ...salesEntity,
      ...values,
      customers: customers.find(it => it.id.toString() === values.customers?.toString()),
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
          saleDate: displayDefaultDateTime(),
        }
      : {
          ...salesEntity,
          saleDate: convertDateTimeFromServer(salesEntity.saleDate),
          customers: salesEntity?.customers?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.sales.home.createOrEditLabel" data-cy="SalesCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.sales.home.createOrEditLabel">Create or edit a Sales</Translate>
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
                  id="sales-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.sales.saleDate')}
                id="sales-saleDate"
                name="saleDate"
                data-cy="saleDate"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.sales.invoiceNumber')}
                id="sales-invoiceNumber"
                name="invoiceNumber"
                data-cy="invoiceNumber"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.sales.totalAmount')}
                id="sales-totalAmount"
                name="totalAmount"
                data-cy="totalAmount"
                type="text"
              />
              <ValidatedField
                id="sales-customers"
                name="customers"
                data-cy="customers"
                label={translate('hmPharmacyApp.sales.customers')}
                type="select"
              >
                <option value="" key="0" />
                {customers
                  ? customers.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/sales" replace color="info">
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

export default SalesUpdate;

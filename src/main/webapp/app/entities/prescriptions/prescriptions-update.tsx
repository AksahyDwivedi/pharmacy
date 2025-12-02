import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getCustomers } from 'app/entities/customers/customers.reducer';
import { createEntity, getEntity, reset, updateEntity } from './prescriptions.reducer';

export const PrescriptionsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const customers = useAppSelector(state => state.customers.entities);
  const prescriptionsEntity = useAppSelector(state => state.prescriptions.entity);
  const loading = useAppSelector(state => state.prescriptions.loading);
  const updating = useAppSelector(state => state.prescriptions.updating);
  const updateSuccess = useAppSelector(state => state.prescriptions.updateSuccess);

  const handleClose = () => {
    navigate('/prescriptions');
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

    const entity = {
      ...prescriptionsEntity,
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
      ? {}
      : {
          ...prescriptionsEntity,
          customers: prescriptionsEntity?.customers?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.prescriptions.home.createOrEditLabel" data-cy="PrescriptionsCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.prescriptions.home.createOrEditLabel">Create or edit a Prescriptions</Translate>
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
                  id="prescriptions-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.prescriptions.doctorName')}
                id="prescriptions-doctorName"
                name="doctorName"
                data-cy="doctorName"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.prescriptions.prescriptionDate')}
                id="prescriptions-prescriptionDate"
                name="prescriptionDate"
                data-cy="prescriptionDate"
                type="date"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.prescriptions.notes')}
                id="prescriptions-notes"
                name="notes"
                data-cy="notes"
                type="textarea"
              />
              <ValidatedField
                id="prescriptions-customers"
                name="customers"
                data-cy="customers"
                label={translate('hmPharmacyApp.prescriptions.customers')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/prescriptions" replace color="info">
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

export default PrescriptionsUpdate;

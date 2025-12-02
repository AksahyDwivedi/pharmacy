import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './suppliers.reducer';

export const SuppliersUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const suppliersEntity = useAppSelector(state => state.suppliers.entity);
  const loading = useAppSelector(state => state.suppliers.loading);
  const updating = useAppSelector(state => state.suppliers.updating);
  const updateSuccess = useAppSelector(state => state.suppliers.updateSuccess);

  const handleClose = () => {
    navigate('/suppliers');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
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
      ...suppliersEntity,
      ...values,
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
          ...suppliersEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmPharmacyApp.suppliers.home.createOrEditLabel" data-cy="SuppliersCreateUpdateHeading">
            <Translate contentKey="hmPharmacyApp.suppliers.home.createOrEditLabel">Create or edit a Suppliers</Translate>
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
                  id="suppliers-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('hmPharmacyApp.suppliers.name')}
                id="suppliers-name"
                name="name"
                data-cy="name"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.suppliers.contactPerson')}
                id="suppliers-contactPerson"
                name="contactPerson"
                data-cy="contactPerson"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.suppliers.phone')}
                id="suppliers-phone"
                name="phone"
                data-cy="phone"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.suppliers.email')}
                id="suppliers-email"
                name="email"
                data-cy="email"
                type="text"
              />
              <ValidatedField
                label={translate('hmPharmacyApp.suppliers.address')}
                id="suppliers-address"
                name="address"
                data-cy="address"
                type="textarea"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/suppliers" replace color="info">
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

export default SuppliersUpdate;

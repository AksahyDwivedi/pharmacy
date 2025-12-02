import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './prescriptions.reducer';

export const PrescriptionsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const prescriptionsEntity = useAppSelector(state => state.prescriptions.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="prescriptionsDetailsHeading">
          <Translate contentKey="hmPharmacyApp.prescriptions.detail.title">Prescriptions</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{prescriptionsEntity.id}</dd>
          <dt>
            <span id="doctorName">
              <Translate contentKey="hmPharmacyApp.prescriptions.doctorName">Doctor Name</Translate>
            </span>
          </dt>
          <dd>{prescriptionsEntity.doctorName}</dd>
          <dt>
            <span id="prescriptionDate">
              <Translate contentKey="hmPharmacyApp.prescriptions.prescriptionDate">Prescription Date</Translate>
            </span>
          </dt>
          <dd>
            {prescriptionsEntity.prescriptionDate ? (
              <TextFormat value={prescriptionsEntity.prescriptionDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="notes">
              <Translate contentKey="hmPharmacyApp.prescriptions.notes">Notes</Translate>
            </span>
          </dt>
          <dd>{prescriptionsEntity.notes}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.prescriptions.customers">Customers</Translate>
          </dt>
          <dd>{prescriptionsEntity.customers ? prescriptionsEntity.customers.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/prescriptions" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/prescriptions/${prescriptionsEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PrescriptionsDetail;

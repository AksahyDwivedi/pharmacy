import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './medicine-batches.reducer';

export const MedicineBatchesDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const medicineBatchesEntity = useAppSelector(state => state.medicineBatches.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="medicineBatchesDetailsHeading">
          <Translate contentKey="hmPharmacyApp.medicineBatches.detail.title">MedicineBatches</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{medicineBatchesEntity.id}</dd>
          <dt>
            <span id="batchNumber">
              <Translate contentKey="hmPharmacyApp.medicineBatches.batchNumber">Batch Number</Translate>
            </span>
          </dt>
          <dd>{medicineBatchesEntity.batchNumber}</dd>
          <dt>
            <span id="expiryDate">
              <Translate contentKey="hmPharmacyApp.medicineBatches.expiryDate">Expiry Date</Translate>
            </span>
          </dt>
          <dd>
            {medicineBatchesEntity.expiryDate ? (
              <TextFormat value={medicineBatchesEntity.expiryDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="quantity">
              <Translate contentKey="hmPharmacyApp.medicineBatches.quantity">Quantity</Translate>
            </span>
          </dt>
          <dd>{medicineBatchesEntity.quantity}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.medicineBatches.purchases">Purchases</Translate>
          </dt>
          <dd>{medicineBatchesEntity.purchases ? medicineBatchesEntity.purchases.id : ''}</dd>
          <dt>
            <Translate contentKey="hmPharmacyApp.medicineBatches.medicines">Medicines</Translate>
          </dt>
          <dd>{medicineBatchesEntity.medicines ? medicineBatchesEntity.medicines.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/medicine-batches" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/medicine-batches/${medicineBatchesEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default MedicineBatchesDetail;

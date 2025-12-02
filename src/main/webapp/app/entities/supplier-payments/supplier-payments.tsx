import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Col, Form, FormGroup, Input, InputGroup, Row, Table } from 'reactstrap';
import { TextFormat, Translate, getSortState, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC } from 'app/shared/util/pagination.constants';
import { overrideSortStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities, searchEntities } from './supplier-payments.reducer';

export const SupplierPayments = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [search, setSearch] = useState('');
  const [sortState, setSortState] = useState(overrideSortStateWithQueryParams(getSortState(pageLocation, 'id'), pageLocation.search));

  const supplierPaymentsList = useAppSelector(state => state.supplierPayments.entities);
  const loading = useAppSelector(state => state.supplierPayments.loading);

  const getAllEntities = () => {
    if (search) {
      dispatch(
        searchEntities({
          query: search,
          sort: `${sortState.sort},${sortState.order}`,
        }),
      );
    } else {
      dispatch(
        getEntities({
          sort: `${sortState.sort},${sortState.order}`,
        }),
      );
    }
  };

  const startSearching = e => {
    if (search) {
      dispatch(
        searchEntities({
          query: search,
          sort: `${sortState.sort},${sortState.order}`,
        }),
      );
    }
    e.preventDefault();
  };

  const clear = () => {
    setSearch('');
    dispatch(getEntities({}));
  };

  const handleSearch = event => setSearch(event.target.value);

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?sort=${sortState.sort},${sortState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [sortState.order, sortState.sort, search]);

  const sort = p => () => {
    setSortState({
      ...sortState,
      order: sortState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = sortState.sort;
    const order = sortState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="supplier-payments-heading" data-cy="SupplierPaymentsHeading">
        <Translate contentKey="hmPharmacyApp.supplierPayments.home.title">Supplier Payments</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="hmPharmacyApp.supplierPayments.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/supplier-payments/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="hmPharmacyApp.supplierPayments.home.createLabel">Create new Supplier Payments</Translate>
          </Link>
        </div>
      </h2>
      <Row>
        <Col sm="12">
          <Form onSubmit={startSearching}>
            <FormGroup>
              <InputGroup>
                <Input
                  type="text"
                  name="search"
                  defaultValue={search}
                  onChange={handleSearch}
                  placeholder={translate('hmPharmacyApp.supplierPayments.home.search')}
                />
                <Button className="input-group-addon">
                  <FontAwesomeIcon icon="search" />
                </Button>
                <Button type="reset" className="input-group-addon" onClick={clear}>
                  <FontAwesomeIcon icon="trash" />
                </Button>
              </InputGroup>
            </FormGroup>
          </Form>
        </Col>
      </Row>
      <div className="table-responsive">
        {supplierPaymentsList && supplierPaymentsList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="hmPharmacyApp.supplierPayments.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('paymentDate')}>
                  <Translate contentKey="hmPharmacyApp.supplierPayments.paymentDate">Payment Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('paymentDate')} />
                </th>
                <th className="hand" onClick={sort('paymentMethod')}>
                  <Translate contentKey="hmPharmacyApp.supplierPayments.paymentMethod">Payment Method</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('paymentMethod')} />
                </th>
                <th className="hand" onClick={sort('paymentStatus')}>
                  <Translate contentKey="hmPharmacyApp.supplierPayments.paymentStatus">Payment Status</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('paymentStatus')} />
                </th>
                <th className="hand" onClick={sort('amountPaid')}>
                  <Translate contentKey="hmPharmacyApp.supplierPayments.amountPaid">Amount Paid</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('amountPaid')} />
                </th>
                <th>
                  <Translate contentKey="hmPharmacyApp.supplierPayments.suppliers">Suppliers</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="hmPharmacyApp.supplierPayments.purchases">Purchases</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {supplierPaymentsList.map((supplierPayments, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/supplier-payments/${supplierPayments.id}`} color="link" size="sm">
                      {supplierPayments.id}
                    </Button>
                  </td>
                  <td>
                    {supplierPayments.paymentDate ? (
                      <TextFormat type="date" value={supplierPayments.paymentDate} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{supplierPayments.paymentMethod}</td>
                  <td>{supplierPayments.paymentStatus}</td>
                  <td>{supplierPayments.amountPaid}</td>
                  <td>
                    {supplierPayments.suppliers ? (
                      <Link to={`/suppliers/${supplierPayments.suppliers.id}`}>{supplierPayments.suppliers.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>
                    {supplierPayments.purchases ? (
                      <Link to={`/purchases/${supplierPayments.purchases.id}`}>{supplierPayments.purchases.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/supplier-payments/${supplierPayments.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/supplier-payments/${supplierPayments.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() => (window.location.href = `/supplier-payments/${supplierPayments.id}/delete`)}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="hmPharmacyApp.supplierPayments.home.notFound">No Supplier Payments found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default SupplierPayments;

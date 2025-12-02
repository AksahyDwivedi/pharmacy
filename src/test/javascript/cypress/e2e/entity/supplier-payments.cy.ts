import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('SupplierPayments e2e test', () => {
  const supplierPaymentsPageUrl = '/supplier-payments';
  const supplierPaymentsPageUrlPattern = new RegExp('/supplier-payments(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const supplierPaymentsSample = {};

  let supplierPayments;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/supplier-payments+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/supplier-payments').as('postEntityRequest');
    cy.intercept('DELETE', '/api/supplier-payments/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (supplierPayments) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/supplier-payments/${supplierPayments.id}`,
      }).then(() => {
        supplierPayments = undefined;
      });
    }
  });

  it('SupplierPayments menu should load SupplierPayments page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('supplier-payments');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('SupplierPayments').should('exist');
    cy.url().should('match', supplierPaymentsPageUrlPattern);
  });

  describe('SupplierPayments page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(supplierPaymentsPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create SupplierPayments page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/supplier-payments/new$'));
        cy.getEntityCreateUpdateHeading('SupplierPayments');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', supplierPaymentsPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/supplier-payments',
          body: supplierPaymentsSample,
        }).then(({ body }) => {
          supplierPayments = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/supplier-payments+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [supplierPayments],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(supplierPaymentsPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details SupplierPayments page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('supplierPayments');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', supplierPaymentsPageUrlPattern);
      });

      it('edit button click should load edit SupplierPayments page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SupplierPayments');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', supplierPaymentsPageUrlPattern);
      });

      it('edit button click should load edit SupplierPayments page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('SupplierPayments');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', supplierPaymentsPageUrlPattern);
      });

      it('last delete button click should delete instance of SupplierPayments', () => {
        cy.intercept('GET', '/api/supplier-payments/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('supplierPayments').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', supplierPaymentsPageUrlPattern);

        supplierPayments = undefined;
      });
    });
  });

  describe('new SupplierPayments page', () => {
    beforeEach(() => {
      cy.visit(`${supplierPaymentsPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('SupplierPayments');
    });

    it('should create an instance of SupplierPayments', () => {
      cy.get(`[data-cy="paymentDate"]`).type('2025-12-01T20:06');
      cy.get(`[data-cy="paymentDate"]`).blur();
      cy.get(`[data-cy="paymentDate"]`).should('have.value', '2025-12-01T20:06');

      cy.get(`[data-cy="paymentMethod"]`).type('via');
      cy.get(`[data-cy="paymentMethod"]`).should('have.value', 'via');

      cy.get(`[data-cy="paymentStatus"]`).type('whup snappy eventually');
      cy.get(`[data-cy="paymentStatus"]`).should('have.value', 'whup snappy eventually');

      cy.get(`[data-cy="amountPaid"]`).type('22520.66');
      cy.get(`[data-cy="amountPaid"]`).should('have.value', '22520.66');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        supplierPayments = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', supplierPaymentsPageUrlPattern);
    });
  });
});

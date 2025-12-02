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

describe('Payments e2e test', () => {
  const paymentsPageUrl = '/payments';
  const paymentsPageUrlPattern = new RegExp('/payments(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const paymentsSample = {};

  let payments;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/payments+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/payments').as('postEntityRequest');
    cy.intercept('DELETE', '/api/payments/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (payments) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/payments/${payments.id}`,
      }).then(() => {
        payments = undefined;
      });
    }
  });

  it('Payments menu should load Payments page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('payments');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Payments').should('exist');
    cy.url().should('match', paymentsPageUrlPattern);
  });

  describe('Payments page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(paymentsPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Payments page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/payments/new$'));
        cy.getEntityCreateUpdateHeading('Payments');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', paymentsPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/payments',
          body: paymentsSample,
        }).then(({ body }) => {
          payments = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/payments+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [payments],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(paymentsPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Payments page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('payments');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', paymentsPageUrlPattern);
      });

      it('edit button click should load edit Payments page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Payments');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', paymentsPageUrlPattern);
      });

      it('edit button click should load edit Payments page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Payments');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', paymentsPageUrlPattern);
      });

      it('last delete button click should delete instance of Payments', () => {
        cy.intercept('GET', '/api/payments/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('payments').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', paymentsPageUrlPattern);

        payments = undefined;
      });
    });
  });

  describe('new Payments page', () => {
    beforeEach(() => {
      cy.visit(`${paymentsPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Payments');
    });

    it('should create an instance of Payments', () => {
      cy.get(`[data-cy="paymentDate"]`).type('2025-12-02T03:48');
      cy.get(`[data-cy="paymentDate"]`).blur();
      cy.get(`[data-cy="paymentDate"]`).should('have.value', '2025-12-02T03:48');

      cy.get(`[data-cy="paymentMethod"]`).type('instead yummy behold');
      cy.get(`[data-cy="paymentMethod"]`).should('have.value', 'instead yummy behold');

      cy.get(`[data-cy="paymentStatus"]`).type('warmhearted limply');
      cy.get(`[data-cy="paymentStatus"]`).should('have.value', 'warmhearted limply');

      cy.get(`[data-cy="amount"]`).type('9480.86');
      cy.get(`[data-cy="amount"]`).should('have.value', '9480.86');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        payments = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', paymentsPageUrlPattern);
    });
  });
});

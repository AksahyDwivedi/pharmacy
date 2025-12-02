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

describe('Suppliers e2e test', () => {
  const suppliersPageUrl = '/suppliers';
  const suppliersPageUrlPattern = new RegExp('/suppliers(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const suppliersSample = {};

  let suppliers;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/suppliers+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/suppliers').as('postEntityRequest');
    cy.intercept('DELETE', '/api/suppliers/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (suppliers) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/suppliers/${suppliers.id}`,
      }).then(() => {
        suppliers = undefined;
      });
    }
  });

  it('Suppliers menu should load Suppliers page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('suppliers');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Suppliers').should('exist');
    cy.url().should('match', suppliersPageUrlPattern);
  });

  describe('Suppliers page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(suppliersPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Suppliers page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/suppliers/new$'));
        cy.getEntityCreateUpdateHeading('Suppliers');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', suppliersPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/suppliers',
          body: suppliersSample,
        }).then(({ body }) => {
          suppliers = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/suppliers+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [suppliers],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(suppliersPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Suppliers page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('suppliers');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', suppliersPageUrlPattern);
      });

      it('edit button click should load edit Suppliers page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Suppliers');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', suppliersPageUrlPattern);
      });

      it('edit button click should load edit Suppliers page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Suppliers');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', suppliersPageUrlPattern);
      });

      it('last delete button click should delete instance of Suppliers', () => {
        cy.intercept('GET', '/api/suppliers/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('suppliers').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', suppliersPageUrlPattern);

        suppliers = undefined;
      });
    });
  });

  describe('new Suppliers page', () => {
    beforeEach(() => {
      cy.visit(`${suppliersPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Suppliers');
    });

    it('should create an instance of Suppliers', () => {
      cy.get(`[data-cy="name"]`).type('boiling but ham');
      cy.get(`[data-cy="name"]`).should('have.value', 'boiling but ham');

      cy.get(`[data-cy="contactPerson"]`).type('mask');
      cy.get(`[data-cy="contactPerson"]`).should('have.value', 'mask');

      cy.get(`[data-cy="phone"]`).type('629-940-3717');
      cy.get(`[data-cy="phone"]`).should('have.value', '629-940-3717');

      cy.get(`[data-cy="email"]`).type('Antone.Kilback@yahoo.com');
      cy.get(`[data-cy="email"]`).should('have.value', 'Antone.Kilback@yahoo.com');

      cy.get(`[data-cy="address"]`).type('../fake-data/blob/hipster.txt');
      cy.get(`[data-cy="address"]`).invoke('val').should('match', new RegExp('../fake-data/blob/hipster.txt'));

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        suppliers = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', suppliersPageUrlPattern);
    });
  });
});

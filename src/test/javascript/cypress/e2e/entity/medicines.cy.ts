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

describe('Medicines e2e test', () => {
  const medicinesPageUrl = '/medicines';
  const medicinesPageUrlPattern = new RegExp('/medicines(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const medicinesSample = {};

  let medicines;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/medicines+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/medicines').as('postEntityRequest');
    cy.intercept('DELETE', '/api/medicines/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (medicines) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/medicines/${medicines.id}`,
      }).then(() => {
        medicines = undefined;
      });
    }
  });

  it('Medicines menu should load Medicines page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('medicines');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Medicines').should('exist');
    cy.url().should('match', medicinesPageUrlPattern);
  });

  describe('Medicines page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(medicinesPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Medicines page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/medicines/new$'));
        cy.getEntityCreateUpdateHeading('Medicines');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicinesPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/medicines',
          body: medicinesSample,
        }).then(({ body }) => {
          medicines = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/medicines+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [medicines],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(medicinesPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Medicines page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('medicines');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicinesPageUrlPattern);
      });

      it('edit button click should load edit Medicines page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Medicines');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicinesPageUrlPattern);
      });

      it('edit button click should load edit Medicines page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Medicines');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicinesPageUrlPattern);
      });

      it('last delete button click should delete instance of Medicines', () => {
        cy.intercept('GET', '/api/medicines/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('medicines').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', medicinesPageUrlPattern);

        medicines = undefined;
      });
    });
  });

  describe('new Medicines page', () => {
    beforeEach(() => {
      cy.visit(`${medicinesPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Medicines');
    });

    it('should create an instance of Medicines', () => {
      cy.get(`[data-cy="name"]`).type('unsung now');
      cy.get(`[data-cy="name"]`).should('have.value', 'unsung now');

      cy.get(`[data-cy="manufacturer"]`).type('from');
      cy.get(`[data-cy="manufacturer"]`).should('have.value', 'from');

      cy.get(`[data-cy="category"]`).type('following whoever');
      cy.get(`[data-cy="category"]`).should('have.value', 'following whoever');

      cy.get(`[data-cy="price"]`).type('32295.9');
      cy.get(`[data-cy="price"]`).should('have.value', '32295.9');

      cy.get(`[data-cy="stock"]`).type('27625');
      cy.get(`[data-cy="stock"]`).should('have.value', '27625');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        medicines = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', medicinesPageUrlPattern);
    });
  });
});

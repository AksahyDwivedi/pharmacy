import React from 'react';
import { Translate } from 'react-jhipster'; // eslint-disable-line

import MenuItem from 'app/shared/layout/menus/menu-item'; // eslint-disable-line

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/medicines">
        <Translate contentKey="global.menu.entities.medicines" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/medicine-batches">
        <Translate contentKey="global.menu.entities.medicineBatches" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/customers">
        <Translate contentKey="global.menu.entities.customers" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/suppliers">
        <Translate contentKey="global.menu.entities.suppliers" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/purchases">
        <Translate contentKey="global.menu.entities.purchases" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/purchase-items">
        <Translate contentKey="global.menu.entities.purchaseItems" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/sales">
        <Translate contentKey="global.menu.entities.sales" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/sale-items">
        <Translate contentKey="global.menu.entities.saleItems" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/prescriptions">
        <Translate contentKey="global.menu.entities.prescriptions" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/payments">
        <Translate contentKey="global.menu.entities.payments" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/supplier-payments">
        <Translate contentKey="global.menu.entities.supplierPayments" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;

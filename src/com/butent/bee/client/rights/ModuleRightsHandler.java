package com.butent.bee.client.rights;

import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.rights.RightsObjectType;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class ModuleRightsHandler extends MultiRoleForm {

  @Override
  public FormInterceptor getInstance() {
    return new ModuleRightsHandler();
  }

  @Override
  protected RightsObjectType getObjectType() {
    return RightsObjectType.MODULE;
  }

  @Override
  protected void initObjects(Consumer<List<RightsObject>> consumer) {
    List<RightsObject> result = new ArrayList<>();

    for (Module module : Module.values()) {
      if (module.isEnabled()) {
        RightsObject mod = new RightsObject(module.getName(), module.getCaption(), module);
        result.add(mod);

        if (!BeeUtils.isEmpty(module.getSubModules())) {
          int cnt = 0;

          for (SubModule subModule : module.getSubModules()) {
            ModuleAndSub ms = ModuleAndSub.of(module, subModule);

            if (ms.isEnabled()) {
              result.add(new RightsObject(subModule.getName(), subModule.getCaption(), ms,
                  1, module.getName()));
              cnt++;
            }
          }

          if (cnt > 0) {
            mod.setHasChildren(true);
          }
        }
      }
    }
    consumer.accept(result);
  }
}

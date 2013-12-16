import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.Admin
import java.util.concurrent.TimeUnit;

service {
    name "sg-validator"
    type "APP_SERVER"
    icon "icon.png"
    elastic false
    numInstances 1
    minAllowedInstances 1
    maxAllowedInstances 2

    compute {
        template "SMALL_LINUX"
    }

    lifecycle{
    }

    customCommands ([
            "get-datagrid-instances" : {gridname,lookuplocators,waitfor->
                Admin admin = new AdminFactory().useDaemonThreads(true).addLocators(lookuplocators).createAdmin();
                admin.getProcessingUnits().waitFor(gridname,3, TimeUnit.MINUTES).waitFor(Integer.valueOf(waitfor));
                return admin.getProcessingUnits().getProcessingUnit(gridname).getInstances().length;
            },
            "get-datagrid-partitions" : {gridname,lookuplocators,waitfor->
                Admin admin = new AdminFactory().useDaemonThreads(true).addLocators(lookuplocators).createAdmin();
                admin.getProcessingUnits().waitFor(gridname,3, TimeUnit.MINUTES).waitFor(Integer.valueOf(waitfor));
                return admin.getProcessingUnits().getProcessingUnit(gridname).getPartitions().length;
            },
            "get-datagrid-backups" : {gridname,lookuplocators,waitfor->
                Admin admin = new AdminFactory().useDaemonThreads(true).addLocators(lookuplocators).createAdmin();
                admin.getProcessingUnits().waitFor(gridname,3, TimeUnit.MINUTES).waitFor(Integer.valueOf(waitfor));
                return admin.getProcessingUnits().getProcessingUnit(gridname).getNumberOfBackups();
            },
            "get-datagrid-deploymentstatus" : {gridname,lookuplocators,waitfor->
                Admin admin = new AdminFactory().useDaemonThreads(true).addLocators(lookuplocators).createAdmin();
                admin.getProcessingUnits().waitFor(gridname,3, TimeUnit.MINUTES).waitFor(Integer.valueOf(waitfor));
                return admin.getProcessingUnits().getProcessingUnit(gridname).getStatus().toString();
            },
            "get-datagrid-maxinstancespermachine" : {gridname,lookuplocators,waitfor->
                Admin admin = new AdminFactory().useDaemonThreads(true).addLocators(lookuplocators).createAdmin();
                admin.getProcessingUnits().waitFor(gridname,3, TimeUnit.MINUTES).waitFor(Integer.valueOf(waitfor));
                return admin.getProcessingUnits().getProcessingUnit(gridname).getMaxInstancesPerMachine();
            },
            "get-datagrid-maxinstancespervm" : {gridname,lookuplocators,waitfor->
                Admin admin = new AdminFactory().useDaemonThreads(true).addLocators(lookuplocators).createAdmin();
                admin.getProcessingUnits().waitFor(gridname,3, TimeUnit.MINUTES).waitFor(Integer.valueOf(waitfor));
                return admin.getProcessingUnits().getProcessingUnit(gridname).getMaxInstancesPerVM();
            }
    ])
}
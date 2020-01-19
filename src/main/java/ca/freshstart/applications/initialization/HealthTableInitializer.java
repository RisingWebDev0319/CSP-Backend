package ca.freshstart.applications.initialization;

import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.healthSection.entity.*;
import ca.freshstart.data.healthSection.repository.HsConditionColumnRepository;
import ca.freshstart.data.healthSection.repository.HsFlagColumnRepository;
import ca.freshstart.data.healthSection.repository.HsSelectColumnRepository;
import ca.freshstart.data.healthSection.repository.HsTableRepository;
import ca.freshstart.data.healthSection.types.HsColumnType;
import ca.freshstart.data.healthSection.types.HsTableType;
import ca.freshstart.data.healthTable.entity.HtColumn;
import ca.freshstart.data.healthTable.entity.HtCustomColumn;
import ca.freshstart.data.healthTable.entity.HtCustomColumnValue;
import ca.freshstart.data.healthTable.repository.HealthTableColumnRepository;
import ca.freshstart.data.healthTable.repository.HealthTableCustomColumnRepository;
import ca.freshstart.data.healthTable.types.HealthTableColumnType;
import ca.freshstart.data.healthTable.types.HealthTableCustomColumnType;
import ca.freshstart.data.serviceCategory.entity.ServiceCategory;
import ca.freshstart.data.serviceCategory.repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class HealthTableInitializer {
    @Autowired
    private HealthTableColumnRepository healthTableColumnRepository;
    @Autowired
    private HealthTableCustomColumnRepository healthTableCustomColumnRepository;
    @Autowired
    private HsTableRepository hsTableRepository;
    @Autowired
    private HsConditionColumnRepository hsConditionColumnRepository;
    @Autowired
    private HsFlagColumnRepository hsFlagColumnRepository;
    @Autowired
    private HsSelectColumnRepository hsSelectColumnRepository;

    @Autowired
    private ServiceCategoryRepository categoryRepository;
    @Autowired
    protected ConcreteCalendarEventRepository concreteCalendarEventRepository;


    public void init() {
        initHealthTable();
        initHealthSections();
    }

    private void initHealthTable() {
        if (healthTableColumnRepository.count() > 0) {
            return;
        }

        {
            HtColumn column1 = new HtColumn();
            column1.setTitle("Name");
            column1.setType(HealthTableColumnType.name);
            column1.setPosition(0);
            column1.setEditable(false);
            column1.setPermanent(true);
            healthTableColumnRepository.save(column1);
        }
        {
            HtColumn column2 = new HtColumn();
            column2.setTitle("Flag");
            column2.setType(HealthTableColumnType.flag);
            column2.setPosition(1);
            column2.setEditable(false);
            column2.setPermanent(true);
            healthTableColumnRepository.save(column2);
        }
        {
            HtColumn column3 = new HtColumn();
            column3.setTitle("Status");
            column3.setType(HealthTableColumnType.status);
            column3.setPosition(2);
            column3.setEditable(false);
            column3.setPermanent(true);
            healthTableColumnRepository.save(column3);
        }
        {
            HtColumn column4 = new HtColumn();
            column4.setTitle("Custom example");
            column4.setType(HealthTableColumnType.custom);
            column4.setPosition(3);

            HtCustomColumn customColumn = new HtCustomColumn();
            customColumn.setTitle("test custom column");
            customColumn.setType(HealthTableCustomColumnType.select);

            HtCustomColumnValue one = new HtCustomColumnValue();
            one.setTitle("one");
            one.setValue(1);
            one.setPosition(0);
            customColumn.addValue(one);
            HtCustomColumnValue two = new HtCustomColumnValue();
            two.setTitle("two");
            two.setValue(2);
            two.setPosition(1);
            customColumn.addValue(two);
            HtCustomColumnValue three = new HtCustomColumnValue();
            three.setTitle("three");
            three.setValue(3);
            three.setPosition(2);
            customColumn.addValue(three);

            customColumn = healthTableCustomColumnRepository.save(customColumn);

            column4.setCustomColumn(customColumn);
            healthTableColumnRepository.save(column4);
        }

    }


    private void initHealthSections() {
        if (hsTableRepository.count() > 0) {
            return;
        }

        // В принципе аналогичны, могут отличаться список Health Conditions но это если пользователь настроит
        HashMap<String, HsTableType> map1 = new HashMap<String, HsTableType>() {{
            put("Physical", HsTableType.physical);
            put("Emotional", HsTableType.emotional);
            put("Structural", HsTableType.structural);
        }};

        for (String title : map1.keySet()) {
            HsTableType sectionType = map1.get(title);

            HsTable healthSection = new HsTable();

            healthSection.setTitle(title);
            healthSection.setType(sectionType);

            // Red, Orange, Green, Yellow
            // можно добавить новые значения, изначально нельзя удалить
            HsColumn flagColumn = new HsColumn();
            flagColumn.setTitle("Flag");
            flagColumn.setType(HsColumnType.flag);
            flagColumn.setPosition(0);
            flagColumn.setPermanent(true);

            HsFlagColumn flagColumn1 = new HsFlagColumn();
            flagColumn1.setSectionType(sectionType);
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Red");
                setColor("red");
                setPermanent(true);
            }});
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Orange");
                setColor("orange");
                setPermanent(true);
            }});
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Green");
                setColor("green");
                setPermanent(true);
            }});
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Yellow");
                setColor("yellow");
                setPermanent(true);
            }});
            hsFlagColumnRepository.save(flagColumn1);

            healthSection.addColumn(flagColumn);

            // изначально значений нет, можно добавить новые
            HsColumn conditionColumn = new HsColumn();
            conditionColumn.setTitle("Condition");
            conditionColumn.setType(HsColumnType.condition);
            conditionColumn.setPosition(1);
            conditionColumn.setPermanent(true);

            HsConditionColumn conditionColumn1 = new HsConditionColumn();
            conditionColumn1.setSectionType(sectionType);
            hsConditionColumnRepository.save(conditionColumn1);

            healthSection.addColumn(conditionColumn);

            // Level - изначально значений нет
            HsColumn levelColumn = new HsColumn();
            levelColumn.setTitle("Level");
            levelColumn.setType(HsColumnType.select);
            levelColumn.setPosition(2);
            levelColumn.setPermanent(true);
            HsSelectColumn selectColumn = new HsSelectColumn();
            selectColumn.setSectionType(sectionType);
            selectColumn = hsSelectColumnRepository.save(selectColumn);
            levelColumn.setSelectColumn(selectColumn);
            healthSection.addColumn(levelColumn);

            HsColumn symptomsColumn = new HsColumn();
            symptomsColumn.setTitle("Symptoms");
            symptomsColumn.setType(HsColumnType.input);
            symptomsColumn.setPosition(3);
            symptomsColumn.setPermanent(true);
            healthSection.addColumn(symptomsColumn);

            HsColumn notesColumn = new HsColumn();
            notesColumn.setTitle("Notes");
            notesColumn.setType(HsColumnType.input);
            notesColumn.setPosition(4);
            notesColumn.setPermanent(true);
            healthSection.addColumn(notesColumn);

            hsTableRepository.save(healthSection);
        }


        HashMap<String, HsTableType> map2 = new HashMap<String, HsTableType>() {{
            put("Custom Protocols", HsTableType.protocols);
            put("Custom Protocols Package", HsTableType.packages);
        }};

        for (String title : map2.keySet()) {
            HsTableType sectionType = map2.get(title);

            HsTable healthSection = new HsTable();
            healthSection.setTitle(title);
            healthSection.setType(sectionType);

            // Выпадающий список
            // todo значения получаются из внешнего сервиса
            // нельзя добавить новые значения
            if (sectionType == HsTableType.protocols) {
                HsColumn protocolsColumn = new HsColumn();
                protocolsColumn.setTitle("Protocols");
                protocolsColumn.setType(HsColumnType.select);
                protocolsColumn.setPosition(0);
                protocolsColumn.setEditable(false);
                protocolsColumn.setPermanent(true);
                HsSelectColumn selectColumn3 = new HsSelectColumn();
                selectColumn3.setSectionType(sectionType);
                selectColumn3 = hsSelectColumnRepository.save(selectColumn3);
                protocolsColumn.setSelectColumn(selectColumn3);
                healthSection.addColumn(protocolsColumn);
            } else if (sectionType == HsTableType.packages) {
                HsColumn protocolsColumn = new HsColumn();
                protocolsColumn.setTitle("Protocols Package");
                protocolsColumn.setType(HsColumnType.select);
                protocolsColumn.setPosition(0);
                protocolsColumn.setEditable(false);
                protocolsColumn.setPermanent(true);
                HsSelectColumn selectColumn3 = new HsSelectColumn();
                selectColumn3.setSectionType(sectionType);
                selectColumn3 = hsSelectColumnRepository.save(selectColumn3);
                protocolsColumn.setSelectColumn(selectColumn3);
                healthSection.addColumn(protocolsColumn);
            }

            // Выпадающий список
            // можно добавить свои
            HsColumn typeColumn = new HsColumn();
            typeColumn.setTitle("Type");
            typeColumn.setType(HsColumnType.select);
            typeColumn.setPosition(1);
            typeColumn.setPermanent(true);
            HsSelectColumn selectColumn2 = new HsSelectColumn();
            selectColumn2.setSectionType(sectionType);
            selectColumn2.addValue(new HsSelectColumnValue() {{
                setTitle("Inhouse Product");
                setValue(1);
                setPosition(0);
                setPermanent(true);
            }});
            selectColumn2.addValue(new HsSelectColumnValue() {{
                setTitle("Product");
                setValue(2);
                setPosition(1);
                setPermanent(true);
            }});
            selectColumn2 = hsSelectColumnRepository.save(selectColumn2);
            typeColumn.setSelectColumn(selectColumn2);
            healthSection.addColumn(typeColumn);

            HsColumn statusColumn = new HsColumn();
            statusColumn.setTitle("Status");
            statusColumn.setType(HsColumnType.select);
            statusColumn.setPosition(2);
            statusColumn.setPermanent(true);
            HsSelectColumn selectColumn1 = new HsSelectColumn();
            selectColumn1.setSectionType(sectionType);
            selectColumn1.addValue(new HsSelectColumnValue() {{
                setTitle("Mandatory");
                setValue(1);
                setPosition(0);
                setPermanent(true);
            }});
            selectColumn1.addValue(new HsSelectColumnValue() {{
                setTitle("In Progress");
                setValue(2);
                setPosition(1);
                setPermanent(true);
            }});
            selectColumn1.addValue(new HsSelectColumnValue() {{
                setTitle("Complete");
                setValue(3);
                setPosition(2);
                setPermanent(true);
            }});
            selectColumn1 = hsSelectColumnRepository.save(selectColumn1);
            statusColumn.setSelectColumn(selectColumn1);
            healthSection.addColumn(statusColumn);

            // todo при смене протокола - заполняется ценой из протокола
            HsColumn costColumn = new HsColumn();
            costColumn.setTitle("Cost to Client");
            costColumn.setType(HsColumnType.input);
            costColumn.setPosition(3);
            costColumn.setPermanent(true);
            healthSection.addColumn(costColumn);

            HsColumn helpColumn = new HsColumn();
            helpColumn.setTitle("To help with");
            helpColumn.setType(HsColumnType.input);
            helpColumn.setPosition(4);
            helpColumn.setPermanent(true);
            healthSection.addColumn(helpColumn);

            HsColumn responsibleColumn = new HsColumn();
            responsibleColumn.setTitle("Responsible");
            responsibleColumn.setType(HsColumnType.input);
            responsibleColumn.setPosition(5);
            responsibleColumn.setPermanent(true);
            healthSection.addColumn(responsibleColumn);

            if (sectionType == HsTableType.protocols) {
                HsColumn directionsColumn = new HsColumn();
                directionsColumn.setTitle("Directions");
                directionsColumn.setType(HsColumnType.input);
                directionsColumn.setPosition(6);
                directionsColumn.setPermanent(true);
                healthSection.addColumn(directionsColumn);
            } else if (sectionType == HsTableType.packages) {
                HsColumn serverColumn = new HsColumn();
                serverColumn.setTitle("Server");
                serverColumn.setType(HsColumnType.select);
                serverColumn.setPosition(6);
                serverColumn.setPermanent(true);
                HsSelectColumn selectColumn = new HsSelectColumn();
                selectColumn.setSectionType(sectionType);
                selectColumn.addValue(new HsSelectColumnValue() {{
                    setTitle("Self-served");
                    setValue(1);
                    setPosition(0);
                    setPermanent(true);
                }});
                selectColumn.addValue(new HsSelectColumnValue() {{
                    setTitle("Stuff");
                    setValue(2);
                    setPosition(1);
                    setPermanent(true);
                }});
                selectColumn.addValue(new HsSelectColumnValue() {{
                    setTitle("therapist");
                    setValue(3);
                    setPosition(2);
                    setPermanent(true);
                }});
                selectColumn = hsSelectColumnRepository.save(selectColumn);
                serverColumn.setSelectColumn(selectColumn);
                healthSection.addColumn(serverColumn);
            }

            HsColumn notesColumn = new HsColumn();
            notesColumn.setTitle("Notes");
            notesColumn.setType(HsColumnType.input);
            notesColumn.setPosition(7);
            notesColumn.setPermanent(true);
            healthSection.addColumn(notesColumn);

            hsTableRepository.save(healthSection);
        }

        {
            HsTable healthSection = new HsTable();
            healthSection.setTitle("Goals");
            HsTableType sectionType = HsTableType.goals;
            healthSection.setType(sectionType);

            // Нередактируемый текст
            // Название поля из которого взят значение
            HsColumn goalFieldColumn = new HsColumn();
            goalFieldColumn.setTitle("Goal Field");
            goalFieldColumn.setType(HsColumnType.text);
            goalFieldColumn.setPosition(0);
            goalFieldColumn.setEditable(false);
            goalFieldColumn.setPermanent(true);
            healthSection.addColumn(goalFieldColumn);

            // Нередактируемый текст
            // Значение поля цели
            HsColumn clientDetailsColumn = new HsColumn();
            clientDetailsColumn.setTitle("Client Details");
            clientDetailsColumn.setType(HsColumnType.text);
            clientDetailsColumn.setPosition(1);
            clientDetailsColumn.setEditable(false);
            clientDetailsColumn.setPermanent(true);
            healthSection.addColumn(clientDetailsColumn);

            // Выпадающий список
            // можно добавлять свои
            HsColumn importanceColumn = new HsColumn();
            importanceColumn.setTitle("Importance");
            importanceColumn.setType(HsColumnType.select);
            importanceColumn.setPosition(2);
            importanceColumn.setPermanent(true);
            HsSelectColumn selectColumn = new HsSelectColumn();
            selectColumn.setSectionType(sectionType);
            selectColumn.addValue(new HsSelectColumnValue() {{
                setTitle("Normal");
                setValue(1);
                setPosition(0);
                setPermanent(true);
            }});
            selectColumn.addValue(new HsSelectColumnValue() {{
                setTitle("High");
                setValue(2);
                setPosition(1);
                setPermanent(true);
            }});
            selectColumn = hsSelectColumnRepository.save(selectColumn);
            importanceColumn.setSelectColumn(selectColumn);
            healthSection.addColumn(importanceColumn);

            HsColumn notesColumn = new HsColumn();
            notesColumn.setTitle("Notes");
            notesColumn.setType(HsColumnType.input);
            notesColumn.setPosition(3);
            notesColumn.setPermanent(true);
            healthSection.addColumn(notesColumn);

            hsTableRepository.save(healthSection);
        }

        {
            HsTable healthSection = new HsTable();
            healthSection.setTitle("Spa Contraindications");
            HsTableType sectionType = HsTableType.contraindications;
            healthSection.setType(sectionType);

            // Выпадающий список
            // изначально значений нет, можно добавить новые
            // данные по здоровью
            HsColumn conditionColumn = new HsColumn();
            conditionColumn.setTitle("Condition");
            conditionColumn.setType(HsColumnType.condition);
            conditionColumn.setPosition(0);
            conditionColumn.setPermanent(true);

            HsConditionColumn conditionColumn1 = new HsConditionColumn();
            conditionColumn1.setSectionType(sectionType);
            hsConditionColumnRepository.save(conditionColumn1);

            HsFlagColumn flagColumn1 = new HsFlagColumn();
            flagColumn1.setSectionType(sectionType);
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Red");
                setColor("red");
                setPermanent(true);
            }});
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Orange");
                setColor("orange");
                setPermanent(true);
            }});
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Green");
                setColor("green");
                setPermanent(true);
            }});
            flagColumn1.addValue(new HsFlagColumnValue() {{
                setTitle("Yellow");
                setColor("yellow");
                setPermanent(true);
            }});
            hsFlagColumnRepository.save(flagColumn1);

            healthSection.addColumn(conditionColumn);

            // Выпадающий список
            // Значения - категории сервисов [и сервисы]
            HsColumn therapyColumn = new HsColumn();
            therapyColumn.setTitle("Therapy");
            therapyColumn.setType(HsColumnType.select);
            therapyColumn.setPosition(1);
            therapyColumn.setEditable(false);
            therapyColumn.setPermanent(true);
            HsSelectColumn selectColumn = new HsSelectColumn();
            selectColumn.setSectionType(sectionType);
            // todo обновить значения в контроллере при запросе
            List<ServiceCategory> categories = categoryRepository.findAll();
            int position = 0;
            for (ServiceCategory serviceCategory : categories) {
                HsSelectColumnValue value = new HsSelectColumnValue();
                value.setTitle(serviceCategory.getName());
                value.setValue(serviceCategory.getId());
                value.setPosition(position++);
                selectColumn.addValue(value);
            }
            selectColumn = hsSelectColumnRepository.save(selectColumn);
            therapyColumn.setSelectColumn(selectColumn);
            healthSection.addColumn(therapyColumn);

            HsColumn statusColumn = new HsColumn();
            statusColumn.setTitle("Status");
            statusColumn.setType(HsColumnType.input);
            statusColumn.setPosition(2);
            statusColumn.setPermanent(true);
            healthSection.addColumn(statusColumn);

            HsColumn reasonColumn = new HsColumn();
            reasonColumn.setTitle("Reason");
            reasonColumn.setType(HsColumnType.input);
            reasonColumn.setPosition(3);
            reasonColumn.setPermanent(true);
            healthSection.addColumn(reasonColumn);

            HsColumn notesColumn = new HsColumn();
            notesColumn.setTitle("Notes");
            notesColumn.setType(HsColumnType.input);
            notesColumn.setPosition(4);
            notesColumn.setPermanent(true);
            healthSection.addColumn(notesColumn);

            hsTableRepository.save(healthSection);
        }
    }


}

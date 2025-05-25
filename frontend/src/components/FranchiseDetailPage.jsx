// src/components/FranchiseDetailPage.jsx
import React, { useEffect, useState, useContext } from 'react';
import {
  Box, Heading, Text, Table, Thead, Tbody, Tr, Th, Td,
  Button, IconButton, useToast, VStack, Input, HStack, SimpleGrid, Select, Flex
} from '@chakra-ui/react';
import { useParams, useNavigate } from 'react-router-dom';
import { EditIcon, DeleteIcon } from '@chakra-ui/icons';
import api from '../api';
import { AuthContext } from '../auth/AuthContext';

const FranchiseDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { token } = useContext(AuthContext);
  const [franchise, setFranchise] = useState(null);
  const [simulationResult, setSimulationResult] = useState(null);
  const [logEntries, setLogEntries] = useState([]);
  const [baseValues, setBaseValues] = useState(null);

  const [editingWorker, setEditingWorker] = useState(null);
  const [selectedWorkers, setSelectedWorkers] = useState({
    marketing: '',
    restructuring: '',
    accounting: ''
  });
  const [newWorker, setNewWorker] = useState({
    name: '', costPerMonth: 0, marketingBonus: 0, restructuringBonus: 0, accountingBonus: 0
  });

  const loadFranchise = async () => {
    const response = await api.get(`/franchise/${id}`);
    setFranchise(response.data);
  };
  const loadLog = async () => {
    try {
      const response = await api.get(`/franchise/${id}/log`);
      setLogEntries(response.data);
    } catch (err) {
      toast({ title: 'Log konnte nicht geladen werden', status: 'error' });
    }
  };



  const loadBaseValues = async () => {
    try {
      const res = await api.get(`/franchise/${id}/base-values`);
      setBaseValues(res.data);
    } catch {
      toast({ title: 'Base-Werte konnten nicht geladen werden', status: 'error' });
    }
  };


  useEffect(() => {
    if (!token || !id) return;
    loadFranchise();
    loadLog();
    loadBaseValues();
  }, [id]);

  const handleWorkerChange = (e) => {
    const { name, value } = e.target;
    setNewWorker(prev => ({ ...prev, [name]: value }));
  };

  const handleSelectChange = (e) => {
    const { name, value } = e.target;
    setSelectedWorkers(prev => ({ ...prev, [name]: value }));
  };

  const handleSimulate = async () => {
    const payload = {
      marketingWorkerId: selectedWorkers.marketing || null,
      restructuringWorkerId: selectedWorkers.restructuring || null,
      accountingWorkerId: selectedWorkers.accounting || null
    };

    try {
      const response = await api.post(`/franchise/${id}/full-simulation`, payload);
      setSimulationResult(response.data);

      toast({ title: 'Simulation abgeschlossen.', status: 'success' });
      await loadFranchise();
      console.log('Simulationsergebnis:', response.data);
    } catch (err) {
      toast({ title: 'Simulation fehlgeschlagen.', status: 'error' });
    }
  };

  const submitWorker = async () => {
    try {
      if (editingWorker) {
        await api.put(`/franchise/${id}/unique-workers/${editingWorker.id}`, newWorker);
      } else {
        await api.post(`/franchise/${id}/unique-workers`, newWorker);
      }
      setNewWorker({ name: '', costPerMonth: 0, marketingBonus: 0, restructuringBonus: 0, accountingBonus: 0 });
      setEditingWorker(null);
      const updated = await api.get(`/franchise/${id}`);
      setFranchise(updated.data);
      loadBaseValues();
    } catch (err) {
      toast({ title: 'Speichern fehlgeschlagen.', status: 'error' });
    }
  };

  const deleteWorker = async (workerId) => {
    try {
      await api.delete(`/franchise/${id}/unique-workers/${workerId}`);
      const updated = await api.get(`/franchise/${id}`);
      setFranchise(updated.data);
      loadBaseValues();
    } catch (err) {
      toast({ title: 'Löschen fehlgeschlagen.', status: 'error' });
    }
  };
  const downloadLogAsCSV = (logEntries) => {
    if (!logEntries || logEntries.length === 0) return;

    const header = [
      "Datum", "Roll", "Modifier", "FinalRoll", "Gewinn", "Beschreibung",
      "MarketingWorker", "RestructuringWorker", "AccountingWorker"
    ];

    const rows = logEntries.map(entry => [
      new Date(entry.timestamp).toLocaleDateString(),
      entry.roll,
      entry.modifier,
      entry.finalRoll,
      entry.profit,
      `"${entry.bookkeepingDescription}"`,
      entry.marketingWorkerName || "",
      entry.restructuringWorkerName || "",
      entry.accountingWorkerName || ""
    ]);

    const csvContent = [header, ...rows]
      .map(e => e.join(","))
      .join("\n");

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = "franchise-journal.csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };


  const buildFranchiseXml = (franchise) => {
    const escapeXml = (str) => str?.toString().replace(/[<>&'\"]/g, c => ({
      '<': '&lt;', '>': '&gt;', '&': '&amp;', "'": '&apos;', '"': '&quot;'
    }[c])) || '';

    const workerXml = franchise.uniqueWorkers.map(worker => `
    <worker>
      <id>${worker.id}</id>
      <name>${escapeXml(worker.name)}</name>
      <costPerMonth>${worker.monthlyCost}</costPerMonth>
      <marketingBonus>${worker.modifierMarketing}</marketingBonus>
      <restructuringBonus>${worker.modifierRestructuring}</restructuringBonus>
      <accountingBonus>${worker.modifierAccounting}</accountingBonus>
    </worker>
  `).join('\n');

    return `<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<franchise>\n  <id>${franchise.id}</id>\n  <name>${escapeXml(franchise.name)}</name>\n  <funds>${franchise.funds}</funds>\n  <propertyValue>${franchise.propertyValue}</propertyValue>\n  <revenueModifier>${franchise.revenueModifier}</revenueModifier>\n  <upkeepModifier>${franchise.upkeepModifier}</upkeepModifier>\n  <unskilledWorkers>${franchise.unskilledWorkers}</unskilledWorkers>\n  <lowskilledWorkers>${franchise.lowskilledWorkers}</lowskilledWorkers>\n  <highskilledWorkers>${franchise.highskilledWorkers}</highskilledWorkers>\n  <costUnskilledWorkers>${franchise.costUnskilledWorkers}</costUnskilledWorkers>\n  <costLowskilledWorkers>${franchise.costLowskilledWorkers}</costLowskilledWorkers>\n  <costHighskilledWorkers>${franchise.costHighskilledWorkers}</costHighskilledWorkers>\n  <uniqueWorkers>\n    ${workerXml}\n  </uniqueWorkers>\n</franchise>`;
  };

  const downloadFranchiseAsXml = () => {
    if (!franchise) return;
    const xmlContent = buildFranchiseXml(franchise);
    const blob = new Blob([xmlContent], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `franchise-${franchise.id}.xml`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const startEdit = (worker) => {
    setEditingWorker(worker);
    setNewWorker({ ...worker });
  };

  if (!franchise) return <Text>Loading...</Text>;

  return (
    <Box maxW="4xl" mx="auto" mt={10}>
      <Flex mb={4} justify="space-between" align="center">
        <Heading>Franchise: {franchise.name}</Heading>
        <Button colorScheme="gray" onClick={() => navigate("/dashboard")}>
          Zurück zum Dashboard
        </Button>
      </Flex>

      <SimpleGrid columns={{ base: 1, md: 2 }} spacingX={6} spacingY={2} mb={6}>
        <Text><b>Funds:</b> {franchise.funds}</Text>
        <Text><b>Property Value:</b> {franchise.propertyValue}</Text>
        <Text><b>Revenue Modifier:</b> {franchise.revenueModifier}</Text>
        <Text><b>Upkeep Modifier:</b> {franchise.upkeepModifier}</Text>
        <Text><b>Unskilled Workers:</b> {franchise.unskilledWorkers}</Text>
        <Text><b>Cost Unskilled Workers:</b> {franchise.costUnskilledWorkers}</Text>
        <Text><b>Low-skilled Workers:</b> {franchise.lowskilledWorkers}</Text>
        <Text><b>Cost Low-skilled Workers:</b> {franchise.costLowskilledWorkers}</Text>
        <Text><b>High-skilled Workers:</b> {franchise.highskilledWorkers}</Text>
        <Text><b>Cost High-skilled Workers:</b> {franchise.costHighskilledWorkers}</Text>
      </SimpleGrid>
      {baseValues && (
        <Text mb={2}>
          📊 <b>Basiswerte:</b> Revenue {baseValues.baseRevenue} \(+ {franchise.revenueModifier}\), Upkeep {baseValues.baseUpkeep} \(+ {franchise.upkeepModifier}\)
        </Text>
      )}


      <HStack mb={4} spacing={4} align="center">
        {['marketing', 'restructuring', 'accounting'].map(type => (
          <Select
            key={type}
            placeholder={`${type.charAt(0).toUpperCase() + type.slice(1)} Worker`}
            name={type}
            onChange={handleSelectChange}
            value={selectedWorkers[type]}
            maxW="200px"
          >
            {franchise.uniqueWorkers.map(worker => (
              <option key={worker.id} value={worker.id}>{worker.name}</option>
            ))}
          </Select>
        ))}
        <Button colorScheme="green" onClick={handleSimulate}>Monat abrechnen</Button>
      </HStack>

      {simulationResult && (
        <Box p={4} borderWidth={1} borderRadius="md" mb={4}>
          <Heading size="sm" mb={2}>Simulationsergebnisse</Heading>

          {simulationResult.marketing && (
            <Text>
              <b>Marketing:</b> {simulationResult.marketing.successes} Erfolg(e),
              DC: {simulationResult.marketing.dc}, neuer RevenueModifier: {simulationResult.marketing.revenueMod}
            </Text>
          )}

          {simulationResult.restructuring && (
            <Text>
              <b>Restrukturierung:</b> {simulationResult.restructuring.successes} Erfolg(e),
              DC: {simulationResult.restructuring.dc}, neuer UpkeepModifier: {simulationResult.restructuring.upkeepMod}
            </Text>
          )}

          {simulationResult.bookkeeping && (
            <Text>
              <b>Monatsabschluss:</b> {simulationResult.bookkeeping.description} (Roll: {simulationResult.bookkeeping.finalRoll}),
              Gewinn: {simulationResult.bookkeeping.finalProfit}, base revenue: {simulationResult.bookkeeping.baseRevenue}, multiplier {simulationResult.bookkeeping.multiplier}, base upkeep {simulationResult.bookkeeping.baseUpkeep}
            </Text>
          )}
        </Box>
      )}


      <Button colorScheme="blue" mb={4} onClick={() => navigate(`/franchise/${franchise.id}/edit`)}>
        Franchise bearbeiten
      </Button>
      <Button colorScheme="gray" onClick={downloadFranchiseAsXml} mb={6}>
        Franchise als XML exportieren
      </Button>

      <Heading size="md" mb={2}>Unique Workers</Heading>
      <Table variant="simple" mb={4}>
        <Thead>
          <Tr>
            <Th>Name</Th><Th>Kosten</Th><Th>Marketing</Th><Th>Restructuring</Th><Th>Accounting</Th><Th>Aktionen</Th>
          </Tr>
        </Thead>
        <Tbody>
          {franchise.uniqueWorkers.map(worker => (
            <Tr key={worker.id}>
              <Td>{worker.name}</Td>
              <Td>{worker.monthlyCost}</Td>
              <Td>{worker.modifierMarketing}</Td>
              <Td>{worker.modifierRestructuring}</Td>
              <Td>{worker.modifierAccounting}</Td>
              <Td>
                <IconButton icon={<EditIcon />} size="sm" mr={2} onClick={() => startEdit(worker)} />
                <IconButton icon={<DeleteIcon />} size="sm" colorScheme="red" onClick={() => deleteWorker(worker.id)} />
              </Td>
            </Tr>
          ))}
        </Tbody>
      </Table>

      <Heading size="sm" mb={2}>{editingWorker ? 'Worker bearbeiten' : 'Neuer Worker'}</Heading>
      <HStack spacing={2} mb={4} wrap="wrap">
        {['name', 'costPerMonth', 'marketingBonus', 'restructuringBonus', 'accountingBonus'].map(key => (
          <Input
            key={key}
            name={key}
            placeholder={key}
            value={newWorker[key] || ''}
            onChange={handleWorkerChange}
            type={key === 'name' ? 'text' : 'number'}
            width="180px"
          />
        ))}
        <Button colorScheme="green" onClick={submitWorker}>
          {editingWorker ? 'Speichern' : 'Hinzufügen'}
        </Button>
      </HStack>
      <Button size="sm" onClick={() => downloadLogAsCSV(logEntries)} mb={2}>
        Journal als CSV exportieren
      </Button>

      {logEntries.length > 0 && (
        <Box mt={10}>
          <Heading size="md" mb={3}>📘 Abrechnungsverlauf</Heading>
          <Table variant="striped" size="sm">
            <Thead>
              <Tr>
                <Th>Datum</Th>
                <Th>Roll</Th>
                <Th>Mod</Th>
                <Th>Final</Th>
                <Th>Gewinn</Th>
                <Th>Beschreibung</Th>
                <Th>Marketing</Th>
                <Th>Restrukturierung</Th>
                <Th>Buchhaltung</Th>
              </Tr>
            </Thead>
            <Tbody>
              {logEntries.map(entry => (
                <Tr key={entry.id}>
                  <Td>{new Date(entry.timestamp).toLocaleDateString()}</Td>
                  <Td>{entry.roll}</Td>
                  <Td>{entry.modifier}</Td>
                  <Td>{entry.finalRoll}</Td>
                  <Td>{entry.profit}</Td>
                  <Td>{entry.bookkeepingDescription}</Td>
                  <Td>{entry.marketingWorkerName || '-'}</Td>
                  <Td>{entry.restructuringWorkerName || '-'}</Td>
                  <Td>{entry.accountingWorkerName || '-'}</Td>
                </Tr>
              ))}
            </Tbody>
          </Table>
        </Box>
      )}

    </Box>


  );

};

export default FranchiseDetailPage;

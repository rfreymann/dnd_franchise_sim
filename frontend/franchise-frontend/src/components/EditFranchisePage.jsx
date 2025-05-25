// src/pages/EditFranchisePage.jsx
import React, { useContext, useEffect, useState } from 'react';
import {
  Box, Heading, FormControl, FormLabel, Input, Button, VStack, useToast, Flex,
} from '@chakra-ui/react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import axios from 'axios';

const EditFranchisePage = () => {
  const { franchises, fetchFranchises } = useContext(AuthContext);
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();

  const franchise = franchises.find((f) => f.id.toString() === id);
  const [formData, setFormData] = useState(franchise || {});

  const editableFields = [
    "name", "funds", "propertyValue",
    "unskilledWorkers", "lowskilledWorkers", "highskilledWorkers",
    "costUnskilledWorkers", "costLowskilledWorkers", "costHighskilledWorkers",
    "revenueModifier", "upkeepModifier"
  ];

  useEffect(() => {
    if (!franchise) {
      fetchFranchises();
    } else {
      setFormData(franchise);
    }
  }, [franchise, fetchFranchises]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.put(`/api/franchise/${id}`, {
        name: formData.name,
        funds: parseInt(formData.funds),
        propertyValue: parseInt(formData.propertyValue),
        unskilledWorkers: parseInt(formData.unskilledWorkers),
        lowskilledWorkers: parseInt(formData.lowskilledWorkers),
        highskilledWorkers: parseInt(formData.highskilledWorkers),
        costUnskilledWorkers: parseInt(formData.costUnskilledWorkers),
        costLowskilledWorkers: parseInt(formData.costLowskilledWorkers),
        costHighskilledWorkers: parseInt(formData.costHighskilledWorkers),
        revenueModifier: parseInt(formData.revenueModifier),
        upkeepModifier: parseInt(formData.upkeepModifier),
      });

      toast({
        title: 'Franchise aktualisiert.',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
      navigate('/dashboard');
    } catch (error) {
      toast({
        title: 'Fehler beim Speichern.',
        description: error.message,
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    }
  };

  return (
    <Box w="full" maxW="800px" mx="auto" mt={10} p={6} borderWidth={1} borderRadius="lg">

        
<Flex mb={6} justify="space-between" align="center">
  <Heading mb={0}>Franchise bearbeiten</Heading>
  <Button
    colorScheme="gray"
    onClick={() => navigate(`/franchise/${id}`)}
  >
    Zurück zur Detailseite
  </Button>
</Flex>

      <form onSubmit={handleSubmit}>
        <VStack spacing={4}>
          {editableFields.map((key) => (
            <FormControl key={key} isRequired>
              <FormLabel htmlFor={key}>{key}</FormLabel>
              <Input
                id={key}
                name={key}
                value={formData[key] || ''}
                onChange={handleChange}
                type={key === 'name' ? 'text' : 'number'}
              />
            </FormControl>
          ))}
          <Button colorScheme="blue" type="submit" width="full">
            Änderungen speichern
          </Button>

        </VStack>
      </form>
    </Box>
  );
};

export default EditFranchisePage;

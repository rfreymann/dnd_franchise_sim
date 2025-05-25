// src/pages/CreateFranchisePage.jsx
import React, { useState, useContext } from 'react';
import {
  Box,
  Heading,
  FormControl,
  FormLabel,
  Input,
  Button,
  VStack,
  useToast,
} from '@chakra-ui/react';
import { AuthContext } from '../auth/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../api';



const CreateFranchisePage = () => {
  const { createFranchise } = useContext(AuthContext);
  const [formData, setFormData] = useState({
    name: '',
    funds: '',
    propertyValue: '',
    unskilledWorkers: '',
    lowskilledWorkers: '',
    highskilledWorkers: '',
    costUnskilledWorkers: '',
    costLowskilledWorkers: '',
    costHighskilledWorkers: '',
    revenueModifier: '',
    upkeepModifier: '',
  });

  const toast = useToast();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };


  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createFranchise({
        ...formData,
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
        title: 'Franchise created.',
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    } catch (error) {
      toast({
        title: 'Error creating franchise.',
        description: error.message,
        status: 'error',
        duration: 3000,
        isClosable: true,
      });
    }
  };

  return (
    <Box maxW="md" mx="auto" mt={10} p={6} borderWidth={1} borderRadius="lg">
      <Heading mb={6}>Create Franchise</Heading>
      <form onSubmit={handleSubmit}>
        <VStack spacing={4}>
          {Object.keys(formData).map((key) => (
            <FormControl key={key} isRequired>
              <FormLabel htmlFor={key}>{key}</FormLabel>
              <Input
                id={key}
                name={key}
                value={formData[key]}
                onChange={handleChange}
                type={key === 'name' ? 'text' : 'number'}
              />
            </FormControl>
          ))}

          <Button colorScheme="blue" type="submit" width="full">
            Create
          </Button>
          <Button colorScheme="gray" onClick={() => navigate('/dashboard')} width="full">
            Back to Dashboard
          </Button>

        </VStack>
      </form>
    </Box>
  );
};

export default CreateFranchisePage;
 
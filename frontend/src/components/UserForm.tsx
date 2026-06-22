import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

import type {
  User,
  UserCreatePayload
} from '../types/user';

//Validation runs before the form is submitted.
const schema = z.object({
  name: z.string().min(1, 'Name is required').max(150, 'Name must not exceed 150 characters'),
  email: z.string().email('Invalid email').max(254, 'Email must not exceed 254 characters'),
  primaryMobile: z.string().regex(/^[6-9]\d{9}$/, 'Must be a valid 10-digit Indian mobile number starting with 6-9'),
  secondaryMobile: z.string().regex(/^[6-9]\d{9}$/, 'Must be a valid 10-digit Indian mobile number').optional().or(z.literal('')),
  aadhaar: z.string().regex(/^[0-9]{12}$/, 'Must be exactly 12 digits'),
  pan: z.string().regex(/^[A-Z]{5}[0-9]{4}[A-Z]$/, 'Format: ABCDE1234F').optional().or(z.literal('')),
  dateOfBirth: z.string().min(1, 'Date of birth is required').refine(date => new Date(date) < new Date(), 'Date of birth must be in the past'),
  placeOfBirth: z.string().max(150, 'Place of birth must not exceed 150 characters').optional().or(z.literal('')),
  currentAddress: z.string().max(500, 'Current address must not exceed 500 characters').optional().or(z.literal('')),
  permanentAddress: z.string().max(500, 'Permanent address must not exceed 500 characters').optional().or(z.literal(''))
});

//Ensures React Hook Form and validation schema always stay in sync.
type FormData = z.infer<typeof schema>;

interface UserFormProps {

  //Existing user when editing. Null/undefined when creating.
  user?: User | null;

  //Callback executed after successful validation.
  onSubmit: (data: UserCreatePayload) => void;

  //Used to disable submit button while API call is running.
  isLoading: boolean;
}

export default function UserForm({ user, onSubmit, isLoading, }: UserFormProps) {

  /**
   * React Hook Form setup.
   * zodResolver integrates Zod validation directly into React Hook Form.
   */
  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  /**
   * Runs whenever:
   * - user changes
   * - reset reference changes
   *
   * Purpose:
   * - Populate form during edit
   * - Clear form during create
   */
  useEffect(() => {

    // Edit Mode
    if (user) {
      reset({
        name: user.name,
        email: user.email,
        primaryMobile: user.primaryMobile,
        secondaryMobile: user.secondaryMobile || '',
        aadhaar: user.aadhaar,
        pan: user.pan,
        dateOfBirth: user.dateOfBirth,
        placeOfBirth: user.placeOfBirth,
        currentAddress: user.currentAddress,
        permanentAddress: user.permanentAddress,
      });
    }

    // Create Mode
    else {
      reset({});
    }

  }, [user, reset]);

  /**
   * Central definition for all form fields.
   *
   * Benefit:
   * - Avoid repeating label/input/error markup
   * - Easy to add/remove fields
   */
  const fields: {
    name: keyof FormData;
    label: string;
    type?: string;
    required?: boolean;
  }[] = [
      { name: 'name', label: 'Full Name', required: true },
      { name: 'email', label: 'Email', type: 'email', required: true },
      { name: 'primaryMobile', label: 'Primary Mobile', required: true },
      { name: 'secondaryMobile', label: 'Secondary Mobile' },
      { name: 'aadhaar', label: 'Aadhaar Number', required: true },
      { name: 'pan', label: 'PAN Number' },
      { name: 'dateOfBirth', label: 'Date of Birth', type: 'date', required: true },

      // Optional according to backend DTO
      { name: 'placeOfBirth', label: 'Place of Birth' },
      { name: 'currentAddress', label: 'Current Address' },
      { name: 'permanentAddress', label: 'Permanent Address' },
    ];

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="form"
      noValidate
    >
      {fields.map(({ name, label, type = 'text', required }) => (
        <div className="form__field" key={name}>

          {/* Input Label */}
          <label className="form__label">
            {label}
            {required && (
              <span className="form__required">*</span>
            )}
          </label>

          {/* Input Field */}
          <input
            {...register(name)}
            type={type}
            className={`form__input ${errors[name]
              ? 'form__input--error'
              : ''
              }`}
          />

          {/* Validation Error */}
          {errors[name] && (
            <span className="form__error">
              {errors[name]?.message}
            </span>
          )}

        </div>
      ))}

      {/* Submit Button */}
      <button
        type="submit"
        className="btn btn--primary btn--full"
        disabled={isLoading}
      >
        {isLoading
          ? 'Saving...'
          : user
            ? 'Update User'
            : 'Create User'}
      </button>
    </form>
  );
}
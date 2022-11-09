import { z } from "zod";
import dateSchema from "./date-schema";

const InstanceSchema = z.object({
  id: z.string(),
  host: z.string(),
  publishedAt: dateSchema.nullable(),
  clientMaxBodyByteSize: z.number().nullable(),
  deletedAt: dateSchema.nullable(),
  createdAt: dateSchema,
  updatedAt: dateSchema
});

type Instance = z.infer<typeof InstanceSchema>;
export type {Instance}

export {InstanceSchema};